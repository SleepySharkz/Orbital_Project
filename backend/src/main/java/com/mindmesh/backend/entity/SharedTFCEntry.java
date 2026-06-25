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
    name = "shared_tfc_entries",
    indexes = {
        @Index(name = "idx_shared_tfc_entries_shared_tfc", columnList = "shared_tfc_id")
    }
)
public class SharedTFCEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shared_tfc_id", nullable = false)
    private SharedTFC sharedTfc;

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

    protected SharedTFCEntry() {
        //JPA stuff
    }

  public SharedTFCEntry(
      SharedTFC sharedTfc,
      Integer displayOrder,
      Long sourceEntryId,
      String flashcardQuestion,
      String flashcardNoteContent,
      String questionText,
      String roughNote,
      LocalDateTime sourceEntryCreatedAt
    ) {
        if (sharedTfc == null) {
        throw new IllegalArgumentException("Shared TFC is required.");
        }

        if (displayOrder == null || displayOrder < 0) {
        throw new IllegalArgumentException("Display order must be non-negative.");
        }

        if (sourceEntryId == null) {
        throw new IllegalArgumentException("Source entry id is required.");
        }

        if (isBlank(flashcardQuestion) || isBlank(flashcardNoteContent) || roughNote == null) {
        throw new IllegalArgumentException("Shared TFC entry content is required.");
        }

        this.sharedTfc = sharedTfc;
        this.displayOrder = displayOrder;
        this.sourceEntryId = sourceEntryId;
        this.flashcardQuestion = flashcardQuestion;
        this.flashcardNoteContent = flashcardNoteContent;
        this.questionText = questionText;
        this.roughNote = roughNote;
        this.sourceEntryCreatedAt = sourceEntryCreatedAt;

        sharedTfc.addEntry(this);
    }

    void setSharedTfc(SharedTFC sharedTfc) {
        this.sharedTfc = sharedTfc;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public Long getId() {
        return id;
    }

    public SharedTFC getSharedTfc() {
        return sharedTfc;
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