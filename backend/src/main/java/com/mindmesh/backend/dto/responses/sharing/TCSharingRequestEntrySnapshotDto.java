package com.mindmesh.backend.dto.responses.sharing;

import java.time.LocalDateTime;

import com.mindmesh.backend.enums.SourceType;

public class TCSharingRequestEntrySnapshotDto {

    private final Long id;
    private final Long sourceEntryId;
    private final String flashcardQuestion;
    private final String flashcardNoteContent;
    private final String questionText;
    private final String roughNote;
    private final SourceType sourceType;
    private final String sourceTitle;
    private final LocalDateTime sourceEntryCreatedAt;

    public TCSharingRequestEntrySnapshotDto(
        Long id,
        Long sourceEntryId,
        String flashcardQuestion,
        String flashcardNoteContent,
        String questionText,
        String roughNote,
        SourceType sourceType,
        String sourceTitle,
        LocalDateTime sourceEntryCreatedAt
    ) {
        this.id = id;
        this.sourceEntryId = sourceEntryId;
        this.flashcardQuestion = flashcardQuestion;
        this.flashcardNoteContent = flashcardNoteContent;
        this.questionText = questionText;
        this.roughNote = roughNote;
        this.sourceType = sourceType;
        this.sourceTitle = sourceTitle;
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

    public String getQuestionText() {
        return questionText;
    }

    public String getRoughNote() {
        return roughNote;
    }

    public SourceType getSourceType() { return sourceType; }
    public String getSourceTitle() { return sourceTitle; }

    public LocalDateTime getSourceEntryCreatedAt() {
        return sourceEntryCreatedAt;
    }
}