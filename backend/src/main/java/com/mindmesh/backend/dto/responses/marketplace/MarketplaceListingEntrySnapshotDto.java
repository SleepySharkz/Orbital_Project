package com.mindmesh.backend.dto.responses.marketplace;

import java.time.LocalDateTime;

public class MarketplaceListingEntrySnapshotDto {

    private final Long id;
    private final Long sourceEntryId;
    private final String flashcardQuestion;
    private final String flashcardNoteContent;
    private final Integer displayOrder;
    private final LocalDateTime sourceEntryCreatedAt;

    public MarketplaceListingEntrySnapshotDto(
        Long id,
        Long sourceEntryId,
        String flashcardQuestion,
        String flashcardNoteContent,
        Integer displayOrder,
        LocalDateTime sourceEntryCreatedAt
    ) {
        this.id = id;
        this.sourceEntryId = sourceEntryId;
        this.flashcardQuestion = flashcardQuestion;
        this.flashcardNoteContent = flashcardNoteContent;
        this.displayOrder = displayOrder;
        this.sourceEntryCreatedAt = sourceEntryCreatedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getSourceEntryId() {
        return sourceEntryId;
    }

    public String getFlashcardQuestion() {
        return flashcardQuestion;
    }

    public String getFlashcardNoteContent() {
        return flashcardNoteContent;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public LocalDateTime getSourceEntryCreatedAt() {
        return sourceEntryCreatedAt;
    }
}
