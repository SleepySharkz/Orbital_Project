package com.mindmesh.backend.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.mindmesh.backend.dto.responses.tfc.TfcContentResponse;
import com.mindmesh.backend.dto.responses.tfc.TfcSummaryResponse;
import com.mindmesh.backend.entity.CFCEntry;
import com.mindmesh.backend.entity.CourseModule;
import com.mindmesh.backend.entity.TFC;
import com.mindmesh.backend.entity.User;
import com.mindmesh.backend.repository.CFCEntryRepository;
import com.mindmesh.backend.repository.CourseModuleRepository;
import com.mindmesh.backend.repository.TFCRepository;

import jakarta.transaction.Transactional;

@Service
public class TFCService {

  private final TFCRepository tfcRepository;
  private final CFCEntryRepository cfcEntryRepository;
  private final CourseModuleRepository courseModuleRepository;

  public TFCService(
      TFCRepository tfcRepository,
      CFCEntryRepository cfcEntryRepository,
      CourseModuleRepository courseModuleRepository) {
    this.tfcRepository = tfcRepository;
    this.cfcEntryRepository = cfcEntryRepository;
    this.courseModuleRepository = courseModuleRepository;
  }

  @Transactional
  public void syncTFCForTopic(CourseModule module, User owner, String topic) {
    Long ownerId = owner.getId();
    Long moduleId = module.getId();

    Optional<TFC> tfcMaybe = tfcRepository.findByOwnerIdAndModuleIdAndTopic(ownerId, moduleId, topic);
    List<CFCEntry> matchingEntries = cfcEntryRepository.findAllByCfcModuleUserIdAndCfcModuleIdAndTopic(
        ownerId,
        moduleId,
        topic);

    if (matchingEntries.isEmpty()) {
      tfcMaybe.ifPresent(tfc -> tfcRepository.delete(tfc));
      return;
    }

    TFC tfc = tfcMaybe.orElseGet(() -> new TFC(module, owner, topic));

    Set<Long> desiredEntryIds = matchingEntries.stream()
        .map(cfcEntry -> cfcEntry.getId())
        .collect(Collectors.toSet());

    List<CFCEntry> currentEntries = new ArrayList<>(tfc.getEntries());
    for (CFCEntry currentEntry : currentEntries) {
      if (!desiredEntryIds.contains(currentEntry.getId())) {
        tfc.removeEntry(currentEntry);
      }
    }

    Set<Long> currentEntryIds = tfc.getEntries().stream()
        .map(cfcEntry -> cfcEntry.getId())
        .collect(Collectors.toSet());

    for (CFCEntry entry : matchingEntries) {
      if (currentEntryIds.contains(entry.getId())) {
        continue;
      }
      tfc.addEntry(entry);
    }

    tfcRepository.save(tfc);
  }

  @Transactional
  public List<TfcSummaryResponse> getTFCsByModule(Long moduleId, Long userId) {
    // 404 status code
    courseModuleRepository.findByIdAndUserId(moduleId, userId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Module not found."));

    // Method to give list of tfcs sorted by update time
    return tfcRepository.findAllByOwnerIdAndModuleIdOrderByUpdatedAtDesc(userId, moduleId)
        .stream()
        .map(this::toTfcSummaryResponse)
        .toList();
  }

  @Transactional
  public TfcContentResponse getTFCById(Long tfcId, Long userId) {
    TFC tfc = tfcRepository.findByIdAndOwnerId(tfcId, userId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "TFC not found."));

    List<TfcContentResponse.TfcEntryView> entries = tfc.getEntries()
        .stream()
        .sorted(Comparator.comparing(CFCEntry::getCreatedAt).reversed())
        .map(this::toTfcEntryView)
        .toList();

    return new TfcContentResponse(
        tfc.getId(),
        tfc.getModule().getId(),
        tfc.getModule().getCourseCode(),
        tfc.getModule().getSchoolSem(),
        tfc.getTopic(),
        tfc.getUpdatedAt(),
        entries);
  }

  private TfcSummaryResponse toTfcSummaryResponse(TFC tfc) {
    return new TfcSummaryResponse(
        tfc.getId(),
        tfc.getModule().getId(),
        tfc.getOwner().getUsername(),
        tfc.getModule().getCourseCode(),
        tfc.getModule().getSchoolSem(),
        tfc.getTopic(),
        tfc.getEntries().size(),
        tfc.getUpdatedAt(),
        null);
  }

  private TfcContentResponse.TfcEntryView toTfcEntryView(CFCEntry entry) {
    return new TfcContentResponse.TfcEntryView(
        entry.getId(),
        entry.getTopic(),
        entry.getGeneratedCFCPage().getFlashcardQuestion(),
        entry.getGeneratedCFCPage().getFlashcardNoteContent(),
        entry.getQuestionText(),
        entry.getRoughNote(),
        entry.getCreatedAt());
  }

}
