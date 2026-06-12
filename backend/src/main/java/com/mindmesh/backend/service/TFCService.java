package com.mindmesh.backend.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.mindmesh.backend.entity.CFCEntry;
import com.mindmesh.backend.entity.CourseModule;
import com.mindmesh.backend.entity.TFC;
import com.mindmesh.backend.entity.User;
import com.mindmesh.backend.repository.CFCEntryRepository;
import com.mindmesh.backend.repository.TFCRepository;

import jakarta.transaction.Transactional;

@Service
public class TFCService {

  private final TFCRepository tfcRepository;
  private final CFCEntryRepository cfcEntryRepository;

  public TFCService(TFCRepository tfcRepository, CFCEntryRepository cfcEntryRepository) {
    this.tfcRepository = tfcRepository;
    this.cfcEntryRepository = cfcEntryRepository;
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

}
