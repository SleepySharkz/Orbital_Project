package com.mindmesh.backend.service;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.mindmesh.backend.dto.responses.sharing.MergeSharedTCResponseDto;
import com.mindmesh.backend.entity.SharedTC;
import com.mindmesh.backend.entity.TC;
import com.mindmesh.backend.enums.SharedTCStatus;
import com.mindmesh.backend.repository.SharedTCRepository;
import com.mindmesh.backend.repository.TCRepository;

@Service
public class SharedTCMergeService {

  private final SharedTCRepository sharedTcRepository;
  private final TCRepository tcRepository;
  private final TCEntryCopyService entryCopyService;
  private final TCUpdateEventPublisher tcUpdateEventPublisher;

  public SharedTCMergeService(
      SharedTCRepository sharedTcRepository,
      TCRepository tcRepository,
      TCEntryCopyService entryCopyService,
      TCUpdateEventPublisher tcUpdateEventPublisher) {
    this.sharedTcRepository = sharedTcRepository;
    this.tcRepository = tcRepository;
    this.entryCopyService = entryCopyService;
    this.tcUpdateEventPublisher = tcUpdateEventPublisher;
  }

  @Transactional
  public MergeSharedTCResponseDto merge(Long sharedTcId, Long ownerId) {
    SharedTC sharedTc = sharedTcRepository.findLockedByIdAndOwnerId(sharedTcId, ownerId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shared TC not found."));
    if (sharedTc.getStatus() != SharedTCStatus.ACTIVE) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Shared TC has already been merged.");
    }

    TC ownedTc = tcRepository.findMatchingOwnedTcForUpdate(
        ownerId, sharedTc.getModule().getId(), sharedTc.getTopic())
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.CONFLICT,
            "Create your own TC for this module and topic before merging."));

    Instant mergedAt = Instant.now();
    int copiedEntryCount = entryCopyService.copySharedEntries(sharedTc, ownedTc, mergedAt);
    sharedTc.markMerged(ownedTc, mergedAt);
    sharedTcRepository.save(sharedTc);
    tcUpdateEventPublisher.publishUpdated(ownedTc);

    return new MergeSharedTCResponseDto(
        ownedTc.getId(), sharedTc.getId(), copiedEntryCount, sharedTc.getStatus(), mergedAt);
  }
}
