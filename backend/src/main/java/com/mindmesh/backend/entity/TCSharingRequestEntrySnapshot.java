package com.mindmesh.backend.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(
    name = "tc_sharing_request_entry_snapshots",
    indexes = {
        @Index(
            name = "idx_tc_sharing_request_entry_snapshots_item_id",
            columnList = "sharing_request_item_id"
        )
    }
)
public class TCSharingRequestEntrySnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sharing_request_item_id", nullable = false)
    private TCSharingRequestItem sharingRequestItem;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(name = "source_entry_id", nullable = false)
    private Long sourceEntryId;

    @Column(name = "flashcard_question", nullable = false, columnDefinition = "TEXT")
    private String flashcardQuestion;

    @Column(name = "flashcard_note_content", nullable = false, columnDefinition = "TEXT")
    private String flashcardNoteContent;

    @Column(name = "question_text", columnDefinition = "TEXT")
    private String questionText;

    @Column(name = "rough_note", nullable = false, columnDefinition = "TEXT")
    private String roughNote;

    @Column(name = "source_entry_created_at")
    private LocalDateTime sourceEntryCreatedAt;

    protected TCSharingRequestEntrySnapshot() {
        // Required by JPA.
    }

    public TCSharingRequestEntrySnapshot(
        TCSharingRequestItem sharingRequestItem,
        Integer displayOrder,
        Long sourceEntryId,
        String flashcardQuestion,
        String flashcardNoteContent,
        String questionText,
        String roughNote,
        LocalDateTime sourceEntryCreatedAt
    ) {
        if (sharingRequestItem == null) {
            throw new IllegalArgumentException("Sharing request item is required.");
        }

        if (displayOrder == null || displayOrder < 0) {
            throw new IllegalArgumentException("Display order must be non-negative.");
        }

        if (sourceEntryId == null) {
            throw new IllegalArgumentException("Source entry ID is required.");
        }

        if (isBlank(flashcardQuestion) || isBlank(flashcardNoteContent) || roughNote == null) {
            throw new IllegalArgumentException("Snapshot content is required.");
        }

        this.sharingRequestItem = sharingRequestItem;
        this.displayOrder = displayOrder;
        this.sourceEntryId = sourceEntryId;
        this.flashcardQuestion = flashcardQuestion;
        this.flashcardNoteContent = flashcardNoteContent;
        this.questionText = questionText;
        this.roughNote = roughNote;
        this.sourceEntryCreatedAt = sourceEntryCreatedAt;

        sharingRequestItem.addEntrySnapshot(this);
    }

    void setSharingRequestItem(TCSharingRequestItem sharingRequestItem) {
        this.sharingRequestItem = sharingRequestItem;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public Long getId() {
        return id;
    }

    public TCSharingRequestItem getSharingRequestItem() {
        return sharingRequestItem;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
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

    public String getQuestionText() {
        return questionText;
    }

    public String getRoughNote() {
        return roughNote;
    }

    public LocalDateTime getSourceEntryCreatedAt() {
        return sourceEntryCreatedAt;
    }
}
