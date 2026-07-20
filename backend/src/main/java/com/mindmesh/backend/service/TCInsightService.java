package com.mindmesh.backend.service;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.mindmesh.backend.dto.ai.AIGeneratedInsightPoint;
import com.mindmesh.backend.dto.ai.AIGeneratedInsightResponse;
import com.mindmesh.backend.dto.ai.AIInsightEntryInput;
import com.mindmesh.backend.dto.ai.AIInsightGenerationRequest;
import com.mindmesh.backend.dto.responses.mindmap.MindmapInsightDetailDto;
import com.mindmesh.backend.dto.responses.mindmap.MindmapInsightDetailDto.InsightPointDto;
import com.mindmesh.backend.entity.CFCEntry;
import com.mindmesh.backend.entity.CourseModule;
import com.mindmesh.backend.entity.GeneratedCFCPage;
import com.mindmesh.backend.entity.TC;
import com.mindmesh.backend.entity.TCInsight;
import com.mindmesh.backend.entity.TCInsightPoint;
import com.mindmesh.backend.entity.User;
import com.mindmesh.backend.enums.TCInsightStatus;
import com.mindmesh.backend.repository.CourseModuleRepository;
import com.mindmesh.backend.repository.TCInsightRepository;
import com.mindmesh.backend.repository.TCRepository;

@Service
public class TCInsightService {

  private static final String GENERATION_FAILURE_MESSAGE =
      "Insight generation failed. Please try again.";

  private final TCInsightRepository tcInsightRepository;
  private final TCRepository tcRepository;
  private final CourseModuleRepository courseModuleRepository;

  public TCInsightService(
      TCInsightRepository tcInsightRepository,
      TCRepository tcRepository,
      CourseModuleRepository courseModuleRepository) {
    this.tcInsightRepository = tcInsightRepository;
    this.tcRepository = tcRepository;
    this.courseModuleRepository = courseModuleRepository;
  }

  @Transactional
  public TCInsight findOrCreateGeneratingInsight(
      Long userId,
      Long moduleId,
      List<Long> selectedTcIds) {
    return reserveValidatedInsight(userId, moduleId, selectedTcIds).insight();
  }

  @Transactional
  public InsightReservation reserveDiscoveryInsight(
      Long userId,
      Long moduleId,
      List<Long> selectedTcIds) {
    ReservationEntity reservation = reserveValidatedInsight(
        userId,
        moduleId,
        selectedTcIds);

    return new InsightReservation(
        reservation.insight().getId(),
        reservation.generationRequired());
  }

  @Transactional(readOnly = true)
  public InsightReservation findExistingReservationAfterConflict(
      Long userId,
      Long moduleId,
      List<Long> selectedTcIds) {
    List<Long> canonicalIds = validateAndNormalizeSelection(selectedTcIds);
    TCInsight insight = tcInsightRepository
        .findByUserIdAndModuleIdAndTcAIdAndTcBId(
            userId,
            moduleId,
            canonicalIds.get(0),
            canonicalIds.get(1))
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.CONFLICT,
            "Could not resolve the concurrent insight request."));

    return new InsightReservation(insight.getId(), false);
  }

  @Transactional(readOnly = true)
  public MindmapInsightDetailDto getInsightDetail(Long insightId, Long userId) {
    TCInsight insight = tcInsightRepository.findDetailByIdAndUserId(insightId, userId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Insight not found."));

    return toDetailDto(insight);
  }

  @Transactional(readOnly = true)
  public Optional<AIInsightGenerationRequest> buildGenerationRequest(Long insightId) {
    TCInsight insight = tcInsightRepository.findById(insightId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Insight not found."));

    if (insight.getStatus() != TCInsightStatus.GENERATING) {
      return Optional.empty();
    }

    List<Long> tcIds = List.of(insight.getTcA().getId(), insight.getTcB().getId());
    Map<Long, TC> tcsById = tcRepository
        .findAllOwnedWithInsightInputsByIdIn(insight.getUser().getId(), tcIds)
        .stream()
        .collect(Collectors.toMap(TC::getId, Function.identity()));

    TC tcA = requireGenerationTc(tcsById, insight.getTcA().getId());
    TC tcB = requireGenerationTc(tcsById, insight.getTcB().getId());
    List<AIInsightEntryInput> entriesA = toAIEntries(tcA);
    List<AIInsightEntryInput> entriesB = toAIEntries(tcB);

    return Optional.of(new AIInsightGenerationRequest(
        insight.getModule().getCourseCode(),
        insight.getModule().getSchoolSem(),
        insight.getTopicA(),
        insight.getTopicB(),
        entriesA,
        entriesB,
        contentHash(tcA.getTopic(), entriesA),
        contentHash(tcB.getTopic(), entriesB)));
  }

  @Transactional
  public void completeGeneration(
      Long insightId,
      AIGeneratedInsightResponse generated,
      AIInsightGenerationRequest request,
      Instant completedAt) {
    TCInsight insight = tcInsightRepository.findById(insightId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Insight not found."));

    if (insight.getStatus() != TCInsightStatus.GENERATING) {
      return;
    }

    if (generated == null || generated.getHasUsefulLink() == null) {
      throw new IllegalArgumentException("Generated insight result is incomplete.");
    }

    if (!generated.getHasUsefulLink()) {
      insight.markNoUsefulLink(
          generated.getRejectionReason(),
          request.tcAContentHash(),
          request.tcBContentHash(),
          completedAt);
      return;
    }

    List<AIGeneratedInsightPoint> generatedPoints = generated.getInsights();
    if (generatedPoints == null || generatedPoints.isEmpty()) {
      throw new IllegalArgumentException("A useful insight requires points.");
    }

    List<TCInsightPoint> points = java.util.stream.IntStream
        .range(0, generatedPoints.size())
        .mapToObj(index -> toEntityPoint(generatedPoints.get(index), index))
        .toList();

    insight.markReady(
        generated.getTitle(),
        generated.getSummary(),
        points,
        request.tcAContentHash(),
        request.tcBContentHash(),
        completedAt);
  }

  @Transactional
  public void markGenerationFailed(Long insightId) {
    tcInsightRepository.findById(insightId)
        .filter(insight -> insight.getStatus() == TCInsightStatus.GENERATING)
        .ifPresent(insight -> insight.markGenerationFailed(GENERATION_FAILURE_MESSAGE));
  }

  private ReservationEntity reserveValidatedInsight(
      Long userId,
      Long moduleId,
      List<Long> selectedTcIds) {
    List<Long> canonicalIds = validateAndNormalizeSelection(selectedTcIds);

    CourseModule module = courseModuleRepository.findByIdAndUserId(moduleId, userId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Module not found."));

    List<TC> selectedTcs = tcRepository
        .findAllOwnedWithInsightInputsByIdIn(userId, canonicalIds)
        .stream()
        .sorted(Comparator.comparing(TC::getId))
        .toList();

    if (selectedTcs.size() != 2) {
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND,
          "One or more selected active TCs were not found.");
    }

    if (selectedTcs.stream().anyMatch(tc -> !tc.getModule().getId().equals(moduleId))) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Both selected TCs must belong to the selected module.");
    }

    Set<String> activeTopics = module.getTopics()
        .stream()
        .map(topic -> normalizeTopic(topic.getTopicName()))
        .collect(Collectors.toSet());

    if (selectedTcs.stream().anyMatch(
        tc -> !activeTopics.contains(normalizeTopic(tc.getTopic())))) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Stale TCs cannot be used to create an insight.");
    }

    TC tcA = selectedTcs.get(0);
    TC tcB = selectedTcs.get(1);
    Optional<TCInsight> existing = tcInsightRepository
        .findByUserIdAndModuleIdAndTcAIdAndTcBId(
            userId,
            moduleId,
            tcA.getId(),
            tcB.getId());

    if (existing.isPresent()) {
      TCInsight insight = existing.get();
      if (insight.getStatus() == TCInsightStatus.GENERATION_FAILED) {
        insight.restartGeneration();
        return new ReservationEntity(insight, true);
      }
      return new ReservationEntity(insight, false);
    }

    User user = module.getUser();
    TCInsight created = tcInsightRepository.saveAndFlush(
        new TCInsight(user, module, tcA, tcB));
    return new ReservationEntity(created, true);
  }

  private List<Long> validateAndNormalizeSelection(List<Long> selectedTcIds) {
    if (selectedTcIds == null
        || selectedTcIds.size() != 2
        || selectedTcIds.stream().anyMatch(id -> id == null)) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Exactly two TC ids are required.");
    }

    if (selectedTcIds.get(0).equals(selectedTcIds.get(1))) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Two different TCs are required.");
    }

    return selectedTcIds.stream().sorted().toList();
  }

  private TC requireGenerationTc(Map<Long, TC> tcsById, Long tcId) {
    TC tc = tcsById.get(tcId);
    if (tc == null || tc.getEntries().isEmpty()) {
      throw new IllegalStateException("Insight input TC is no longer available.");
    }
    return tc;
  }

  private List<AIInsightEntryInput> toAIEntries(TC tc) {
    return tc.getEntries()
        .stream()
        .sorted(Comparator.comparing(CFCEntry::getId))
        .map(this::toAIEntry)
        .toList();
  }

  private AIInsightEntryInput toAIEntry(CFCEntry entry) {
    GeneratedCFCPage generated = entry.getGeneratedCFCPage();
    return new AIInsightEntryInput(
        entry.getId(),
        generated == null ? null : generated.getFlashcardQuestion(),
        generated == null ? null : generated.getFlashcardNoteContent(),
        entry.getQuestionText(),
        entry.getRoughNote(),
        entry.getCfc().getSourceType().name(),
        entry.getCfc().getSourceTitle());
  }

  private String contentHash(String topic, List<AIInsightEntryInput> entries) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      updateDigest(digest, topic);
      for (AIInsightEntryInput entry : entries) {
        updateDigest(digest, entry.entryId() == null ? null : entry.entryId().toString());
        updateDigest(digest, entry.generatedQuestion());
        updateDigest(digest, entry.generatedNote());
        updateDigest(digest, entry.originalQuestion());
        updateDigest(digest, entry.roughNote());
        updateDigest(digest, entry.sourceType());
        updateDigest(digest, entry.sourceTitle());
      }
      return HexFormat.of().formatHex(digest.digest());
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException("SHA-256 is unavailable.", exception);
    }
  }

  private void updateDigest(MessageDigest digest, String value) {
    if (value == null) {
      digest.update(ByteBuffer.allocate(Integer.BYTES).putInt(-1).array());
      return;
    }

    byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
    digest.update(ByteBuffer.allocate(Integer.BYTES).putInt(bytes.length).array());
    digest.update(bytes);
  }

  private TCInsightPoint toEntityPoint(AIGeneratedInsightPoint point, int displayOrder) {
    return new TCInsightPoint(
        point.getHeading(),
        point.getExplanation(),
        point.getSourceTopicAReferences(),
        point.getSourceTopicBReferences(),
        displayOrder);
  }

  private MindmapInsightDetailDto toDetailDto(TCInsight insight) {
    List<InsightPointDto> points = insight.getPoints()
        .stream()
        .map(point -> new InsightPointDto(
            point.getHeading(),
            point.getExplanation(),
            point.getSourceTopicAReferences(),
            point.getSourceTopicBReferences(),
            point.getDisplayOrder()))
        .toList();

    return new MindmapInsightDetailDto(
        insight.getId(),
        insight.getModule().getId(),
        insight.getTcA().getId(),
        insight.getTcB().getId(),
        insight.getTopicA(),
        insight.getTopicB(),
        insight.getStatus(),
        insight.getTitle(),
        insight.getSummary(),
        points,
        insight.getRejectionReason(),
        insight.getCreatedAt(),
        insight.getUpdatedAt(),
        insight.getGeneratedAt());
  }

  private String normalizeTopic(String topic) {
    return topic.trim().toLowerCase(Locale.ROOT);
  }

  public record InsightReservation(Long insightId, boolean generationRequired) {
  }

  private record ReservationEntity(TCInsight insight, boolean generationRequired) {
  }
}