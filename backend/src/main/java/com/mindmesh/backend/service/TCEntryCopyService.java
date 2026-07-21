package com.mindmesh.backend.service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.mindmesh.backend.entity.CFC;
import com.mindmesh.backend.entity.CFCEntry;
import com.mindmesh.backend.entity.GeneratedCFCPage;
import com.mindmesh.backend.entity.SharedTC;
import com.mindmesh.backend.entity.SharedTCEntry;
import com.mindmesh.backend.entity.TC;
import com.mindmesh.backend.enums.SourceType;
import com.mindmesh.backend.repository.CFCRepository;

@Service
public class TCEntryCopyService {

  private final CFCRepository cfcRepository;

  public TCEntryCopyService(CFCRepository cfcRepository) { this.cfcRepository = cfcRepository; }

  public int copySharedEntries(SharedTC sharedTc, TC ownedTc, Instant mergedAt) {
    validateRelationships(sharedTc, ownedTc, mergedAt);
    CFC importedCfc = new CFC(
        ownedTc.getModule(),
        SourceType.SHARED_TC,
        "Private share from " + sharedTc.getOriginalOwnerUsername(),
        "Shared " + ownedTc.getTopic() + " notes",
        "Imported from a private TC shared by " + sharedTc.getOriginalOwnerUsername() + ".");

    List<SharedTCEntry> sourceEntries = sharedTc.getEntries().stream()
        .sorted(Comparator.comparing(SharedTCEntry::getDisplayOrder)
            .thenComparing(SharedTCEntry::getId))
        .toList();

    for (int index = 0; index < sourceEntries.size(); index++) {
      SharedTCEntry source = sourceEntries.get(index);
      CFCEntry copied = new CFCEntry(
          importedCfc,
          (long) index + 1,
          ownedTc.getTopic(),
          source.getQuestionText(),
          source.getRoughNote(),
          new GeneratedCFCPage(source.getFlashcardQuestion(), source.getFlashcardNoteContent()));
      copied.recordSharedOrigin(
          sharedTc.getOriginalOwnerUsername(),
          source.getSourceType(),
          source.getSourceTitle(),
          sharedTc.getId(),
          source.getId(),
          source.getSourceEntryCreatedAt(),
          mergedAt);
      ownedTc.addEntry(copied);
    }

    cfcRepository.saveAndFlush(importedCfc);
    return sourceEntries.size();
  }

  private void validateRelationships(SharedTC sharedTc, TC ownedTc, Instant mergedAt) {
    if (sharedTc == null || sharedTc.getId() == null
        || ownedTc == null || ownedTc.getId() == null || mergedAt == null) {
      throw new IllegalArgumentException("Persisted merge inputs are required.");
    }
    if (!sharedTc.getOwner().getId().equals(ownedTc.getOwner().getId())
        || !sharedTc.getModule().getId().equals(ownedTc.getModule().getId())) {
      throw new IllegalArgumentException("Shared and owned TCs must have the same owner and module.");
    }
  }
}
