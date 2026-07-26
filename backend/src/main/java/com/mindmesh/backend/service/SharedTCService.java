package com.mindmesh.backend.service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.mindmesh.backend.dto.responses.sharing.SharedTCDetailDto;
import com.mindmesh.backend.dto.responses.sharing.SharedTCEntryDto;
import com.mindmesh.backend.dto.responses.sharing.SharedTCSummaryDto;
import com.mindmesh.backend.entity.SharedTC;
import com.mindmesh.backend.entity.SharedTCEntry;
import com.mindmesh.backend.entity.TC;
import com.mindmesh.backend.enums.SharedTCStatus;
import com.mindmesh.backend.repository.SharedTCRepository;
import com.mindmesh.backend.repository.TCRepository;

@Service
public class SharedTCService {

  private static final String MISSING_OWNED_TC_MESSAGE =
      "Create your own TC for this module and topic before merging.";

  private final SharedTCRepository sharedTcRepository;
  private final TCRepository tcRepository;

  public SharedTCService(SharedTCRepository sharedTcRepository, TCRepository tcRepository) {
    this.sharedTcRepository = sharedTcRepository;
    this.tcRepository = tcRepository;
  }

  @Transactional(readOnly = true)
  public List<SharedTCSummaryDto> listSharedTcs(Long ownerId) {
    return sharedTcRepository.findActiveByOwnerId(ownerId, SharedTCStatus.ACTIVE)
        .stream().map(this::toSummaryDto).toList();
  }

  @Transactional(readOnly = true)
  public SharedTCDetailDto getSharedTcById(Long sharedTcId, Long ownerId) {
    SharedTC sharedTc = sharedTcRepository.findByIdAndOwnerId(sharedTcId, ownerId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shared TC not found."));
    return toDetailDto(sharedTc, ownerId);
  }

  private SharedTCSummaryDto toSummaryDto(SharedTC sharedTc) {
    return new SharedTCSummaryDto(
        sharedTc.getId(),
        sharedTc.getModule().getId(),
        sharedTc.getCourseCode(),
        sharedTc.getSchoolSem(),
        sharedTc.getTopic(),
        sharedTc.getEntries().size(),
        sharedTc.getOriginalOwner().getId(),
        sharedTc.getOriginalOwnerUsername(),
        sharedTc.getAcceptedAt());
  }

  private SharedTCDetailDto toDetailDto(SharedTC sharedTc, Long ownerId) {
    MergeEligibility eligibility = mergeEligibility(sharedTc, ownerId);
    TC mergedInto = sharedTc.getMergedIntoTc();
    return new SharedTCDetailDto(
        sharedTc.getId(),
        sharedTc.getModule().getId(),
        sharedTc.getCourseCode(),
        sharedTc.getSchoolSem(),
        sharedTc.getTopic(),
        sharedTc.getOriginalOwner().getId(),
        sharedTc.getOriginalOwnerUsername(),
        sharedTc.getAcceptedAt(),
        sharedTc.getStatus(),
        eligibility.matchingOwnedTcId(),
        eligibility.canMerge(),
        eligibility.blockingReason(),
        mergedInto == null ? null : mergedInto.getId(),
        sharedTc.getMergedAt(),
        sortedEntries(sharedTc).stream().map(this::toEntryDto).toList());
  }

  private MergeEligibility mergeEligibility(SharedTC sharedTc, Long ownerId) {
    if (sharedTc.getStatus() != SharedTCStatus.ACTIVE) {
      Long mergedIntoTcId = sharedTc.getMergedIntoTc() == null
          ? null : sharedTc.getMergedIntoTc().getId();
      return new MergeEligibility(mergedIntoTcId, false, "This shared TC has already been merged.");
    }
    Optional<TC> matching = tcRepository.findMatchingOwnedTc(
        ownerId, sharedTc.getModule().getId(), sharedTc.getTopic());
    return matching
        .map(tc -> new MergeEligibility(tc.getId(), true, null))
        .orElseGet(() -> new MergeEligibility(null, false, MISSING_OWNED_TC_MESSAGE));
  }

  private SharedTCEntryDto toEntryDto(SharedTCEntry entry) {
    return new SharedTCEntryDto(
        entry.getId(),
        entry.getSourceEntryId(),
        entry.getFlashcardQuestion(),
        entry.getFlashcardNoteContent(),
        entry.getQuestionText(),
        entry.getRoughNote(),
        entry.getSourceType(),
        entry.getSourceTitle(),
        entry.getSourceEntryCreatedAt());
  }

  private List<SharedTCEntry> sortedEntries(SharedTC sharedTc) {
    return sharedTc.getEntries().stream()
        .sorted(Comparator.comparing(SharedTCEntry::getDisplayOrder)
            .thenComparing(SharedTCEntry::getId))
        .toList();
  }

  private record MergeEligibility(Long matchingOwnedTcId, boolean canMerge, String blockingReason) {
  }
}
