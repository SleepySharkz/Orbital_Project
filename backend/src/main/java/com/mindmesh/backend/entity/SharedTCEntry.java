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
    name = "shared_tc_entries",
    indexes = {
        @Index(name = "idx_shared_tc_entries_shared_tc", columnList = "shared_tc_id")
    }
)
public class SharedTCEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shared_tc_id", nullable = false)
    private SharedTC sharedTc;

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

    protected SharedTCEntry() {
        //JPA stuff
    }

  public SharedTCEntry(
      SharedTC sharedTc,
      Integer displayOrder,
      Long sourceEntryId,
      String flashcardQuestion,
      String flashcardNoteContent,
      String questionText,
      String roughNote,
      LocalDateTime sourceEntryCreatedAt
    ) {
        if (sharedTc == null) {
        throw new IllegalArgumentException("Shared TC is required.");
        }

        if (displayOrder == null || displayOrder < 0) {
        throw new IllegalArgumentException("Display order must be non-negative.");
        }

        if (sourceEntryId == null) {
        throw new IllegalArgumentException("Source entry id is required.");
        }

        if (isBlank(flashcardQuestion) || isBlank(flashcardNoteContent) || roughNote == null) {
        throw new IllegalArgumentException("Shared TC entry content is required.");
        }

        this.sharedTc = sharedTc;
        this.displayOrder = displayOrder;
        this.sourceEntryId = sourceEntryId;
        this.flashcardQuestion = flashcardQuestion;
        this.flashcardNoteContent = flashcardNoteContent;
        this.questionText = questionText;
        this.roughNote = roughNote;
        this.sourceEntryCreatedAt = sourceEntryCreatedAt;

        sharedTc.addEntry(this);
    }

    void setSharedTc(SharedTC sharedTc) {
        this.sharedTc = sharedTc;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public Long getId() {
        return id;
    }

    public SharedTC getSharedTc() {
        return sharedTc;
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