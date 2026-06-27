package com.mindmesh.backend.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.mindmesh.backend.dto.responses.tc.TcContentResponse;
import com.mindmesh.backend.dto.responses.tc.TcSummaryResponse;
import com.mindmesh.backend.entity.CFCEntry;
import com.mindmesh.backend.entity.CourseModule;
import com.mindmesh.backend.entity.TC;
import com.mindmesh.backend.entity.User;
import com.mindmesh.backend.repository.CFCEntryRepository;
import com.mindmesh.backend.repository.CourseModuleRepository;
import com.mindmesh.backend.repository.TCRepository;

import jakarta.transaction.Transactional;

@Service
public class TCService {

  private final TCRepository tcRepository;
  private final CFCEntryRepository cfcEntryRepository;
  private final CourseModuleRepository courseModuleRepository;

  public TCService(
      TCRepository tcRepository,
      CFCEntryRepository cfcEntryRepository,
      CourseModuleRepository courseModuleRepository) {
    this.tcRepository = tcRepository;
    this.cfcEntryRepository = cfcEntryRepository;
    this.courseModuleRepository = courseModuleRepository;
  }

  @Transactional
  public void syncTCForTopic(CourseModule module, User owner, String topic) {
    Long ownerId = owner.getId();
    Long moduleId = module.getId();

    Optional<TC> tcMaybe = tcRepository.findByOwnerIdAndModuleIdAndTopic(ownerId, moduleId, topic);
    List<CFCEntry> matchingEntries = cfcEntryRepository.findAllByCfcModuleUserIdAndCfcModuleIdAndTopic(
        ownerId,
        moduleId,
        topic);

    if (matchingEntries.isEmpty()) {
      tcMaybe.ifPresent(tc -> tcRepository.delete(tc));
      return;
    }

    TC tc = tcMaybe.orElseGet(() -> new TC(module, owner, topic));

    Set<Long> desiredEntryIds = matchingEntries.stream()
        .map(cfcEntry -> cfcEntry.getId())
        .collect(Collectors.toSet());

    List<CFCEntry> currentEntries = new ArrayList<>(tc.getEntries());
    for (CFCEntry currentEntry : currentEntries) {
      if (!desiredEntryIds.contains(currentEntry.getId())) {
        tc.removeEntry(currentEntry);
      }
    }

    Set<Long> currentEntryIds = tc.getEntries().stream()
        .map(cfcEntry -> cfcEntry.getId())
        .collect(Collectors.toSet());

    for (CFCEntry entry : matchingEntries) {
      if (currentEntryIds.contains(entry.getId())) {
        continue;
      }
      tc.addEntry(entry);
    }

    tcRepository.save(tc);
  }

  @Transactional
  public List<TcSummaryResponse> getTCsByModule(Long moduleId, Long userId) {
    // 404 status code
    courseModuleRepository.findByIdAndUserId(moduleId, userId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Module not found."));

    // Method to give list of tcs sorted by update time
    return tcRepository.findAllByOwnerIdAndModuleIdOrderByUpdatedAtDesc(userId, moduleId)
        .stream()
        .map(this::toTcSummaryResponse)
        .toList();
  }

  @Transactional
  public TcContentResponse getTCById(Long tcId, Long userId) {
    TC tc = tcRepository.findByIdAndOwnerId(tcId, userId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "TC not found."));
    Boolean isStale = isTcStale(tc);

    List<TcContentResponse.TcEntryView> entries = tc.getEntries()
        .stream()
        .sorted(Comparator.comparing(CFCEntry::getCreatedAt).reversed())
        .map(this::toTcEntryView)
        .toList();

    return new TcContentResponse(
        tc.getId(),
        tc.getModule().getId(),
        tc.getModule().getCourseCode(),
        tc.getModule().getSchoolSem(),
        tc.getTopic(),
        isStale,
        tc.getUpdatedAt(),
        entries);
  }

  private TcSummaryResponse toTcSummaryResponse(TC tc) {
    return new TcSummaryResponse(
        tc.getId(),
        tc.getModule().getId(),
        tc.getOwner().getUsername(),
        tc.getModule().getCourseCode(),
        tc.getModule().getSchoolSem(),
        tc.getTopic(),
        tc.getEntries().size(),
        tc.getUpdatedAt(),
        isTcStale(tc));
  }

  private Boolean isTcStale(TC tc) {
    String tcTopic = tc.getTopic().trim().toLowerCase(Locale.ROOT);

    return tc.getModule()
        .getTopics()
        .stream()
        .map(moduleTopic -> moduleTopic.getTopicName().trim().toLowerCase(Locale.ROOT))
        .noneMatch(topicName -> topicName.equals(tcTopic));
  }

  private TcContentResponse.TcEntryView toTcEntryView(CFCEntry entry) {
    return new TcContentResponse.TcEntryView(
        entry.getId(),
        entry.getTopic(),
        entry.getGeneratedCFCPage().getFlashcardQuestion(),
        entry.getGeneratedCFCPage().getFlashcardNoteContent(),
        entry.getQuestionText(),
        entry.getRoughNote(),
        entry.getCreatedAt());
  }

}
