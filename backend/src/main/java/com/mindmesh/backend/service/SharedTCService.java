package com.mindmesh.backend.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.mindmesh.backend.dto.responses.sharing.SharedTCDetailDto;
import com.mindmesh.backend.dto.responses.sharing.SharedTCEntryDto;
import com.mindmesh.backend.dto.responses.sharing.SharedTCSummaryDto;
import com.mindmesh.backend.entity.SharedTC;
import com.mindmesh.backend.entity.SharedTCEntry;
import com.mindmesh.backend.repository.SharedTCRepository;

@Service
public class SharedTCService {

    private final SharedTCRepository sharedTcRepository;

    public SharedTCService(SharedTCRepository sharedTcRepository) {
        this.sharedTcRepository = sharedTcRepository;
    }

    @Transactional(readOnly = true)
    public List<SharedTCSummaryDto> listSharedTcs(Long ownerId) {
        return sharedTcRepository
            .findByOwnerIdOrderByAcceptedAtDesc(ownerId)
            .stream()
            .map(this::toSummaryDto)
            .toList();
    }

    @Transactional(readOnly = true)
    public SharedTCDetailDto getSharedTcById(Long sharedTcId, Long ownerId) {
        SharedTC sharedTc = sharedTcRepository
            .findByIdAndOwnerId(sharedTcId, ownerId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Shared TC not found."));

        return toDetailDto(sharedTc);
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

    private SharedTCDetailDto toDetailDto(SharedTC sharedTc) {
        return new SharedTCDetailDto(
            sharedTc.getId(),
            sharedTc.getModule().getId(),
            sharedTc.getCourseCode(),
            sharedTc.getSchoolSem(),
            sharedTc.getTopic(),
            sharedTc.getOriginalOwner().getId(),
            sharedTc.getOriginalOwnerUsername(),
            sharedTc.getAcceptedAt(),
            sortedEntries(sharedTc).stream().map(this::toEntryDto).toList());
    }

    private SharedTCEntryDto toEntryDto(SharedTCEntry entry) {
        return new SharedTCEntryDto(
            entry.getId(),
            entry.getSourceEntryId(),
            entry.getFlashcardQuestion(),
            entry.getFlashcardNoteContent(),
            entry.getQuestionText(),
            entry.getRoughNote(),
            entry.getSourceEntryCreatedAt());
    }

    private List<SharedTCEntry> sortedEntries(SharedTC sharedTc) {
        return sharedTc
            .getEntries()
            .stream()
            .sorted(Comparator.comparing(SharedTCEntry::getDisplayOrder))
            .toList();
    }
}
