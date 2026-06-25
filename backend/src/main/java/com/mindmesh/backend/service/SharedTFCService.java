package com.mindmesh.backend.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.mindmesh.backend.dto.responses.sharing.SharedTFCDetailDto;
import com.mindmesh.backend.dto.responses.sharing.SharedTFCEntryDto;
import com.mindmesh.backend.dto.responses.sharing.SharedTFCSummaryDto;
import com.mindmesh.backend.entity.SharedTFC;
import com.mindmesh.backend.entity.SharedTFCEntry;
import com.mindmesh.backend.repository.SharedTFCRepository;

@Service
public class SharedTFCService {

    private final SharedTFCRepository sharedTfcRepository;

    public SharedTFCService(SharedTFCRepository sharedTfcRepository) {
        this.sharedTfcRepository = sharedTfcRepository;
    }

    @Transactional(readOnly = true)
    public List<SharedTFCSummaryDto> listSharedTfcs(Long ownerId) {
        return sharedTfcRepository
            .findByOwnerIdOrderByAcceptedAtDesc(ownerId)
            .stream()
            .map(this::toSummaryDto)
            .toList();
    }

    @Transactional(readOnly = true)
    public SharedTFCDetailDto getSharedTfcById(Long sharedTfcId, Long ownerId) {
        SharedTFC sharedTfc = sharedTfcRepository
            .findByIdAndOwnerId(sharedTfcId, ownerId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Shared TFC not found."));

        return toDetailDto(sharedTfc);
    }

    private SharedTFCSummaryDto toSummaryDto(SharedTFC sharedTfc) {
        return new SharedTFCSummaryDto(
            sharedTfc.getId(),
            sharedTfc.getModule().getId(),
            sharedTfc.getCourseCode(),
            sharedTfc.getSchoolSem(),
            sharedTfc.getTopic(),
            sharedTfc.getEntries().size(),
            sharedTfc.getOriginalOwner().getId(),
            sharedTfc.getOriginalOwnerUsername(),
            sharedTfc.getAcceptedAt());
    }

    private SharedTFCDetailDto toDetailDto(SharedTFC sharedTfc) {
        return new SharedTFCDetailDto(
            sharedTfc.getId(),
            sharedTfc.getModule().getId(),
            sharedTfc.getCourseCode(),
            sharedTfc.getSchoolSem(),
            sharedTfc.getTopic(),
            sharedTfc.getOriginalOwner().getId(),
            sharedTfc.getOriginalOwnerUsername(),
            sharedTfc.getAcceptedAt(),
            sortedEntries(sharedTfc).stream().map(this::toEntryDto).toList());
    }

    private SharedTFCEntryDto toEntryDto(SharedTFCEntry entry) {
        return new SharedTFCEntryDto(
            entry.getId(),
            entry.getSourceEntryId(),
            entry.getFlashcardQuestion(),
            entry.getFlashcardNoteContent(),
            entry.getQuestionText(),
            entry.getRoughNote(),
            entry.getSourceEntryCreatedAt());
    }

    private List<SharedTFCEntry> sortedEntries(SharedTFC sharedTfc) {
        return sharedTfc
            .getEntries()
            .stream()
            .sorted(Comparator.comparing(SharedTFCEntry::getDisplayOrder))
            .toList();
    }
}
