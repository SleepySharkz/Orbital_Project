package com.mindmesh.backend.dto.requests.cfc;

import jakarta.validation.constraints.NotBlank;

public class UpdateCFCEntryContentRequestDto {
    @NotBlank(message = "Flashcard question is required")
    private String flashcardQuestion;

    @NotBlank(message = "Flashcard note content is required")
    private String flashcardNoteContent;

    public UpdateCFCEntryContentRequestDto() {
    }

    public String getFlashcardQuestion() {
        return this.flashcardQuestion;
    }

    public void setFlashcardQuestion(String flashcardQuestion) {
        this.flashcardQuestion = flashcardQuestion;
    }

    public String getFlashcardNoteContent() {
        return this.flashcardNoteContent;
    }

    public void setFlashcardNoteContent(String flashcardNoteContent) {
        this.flashcardNoteContent = flashcardNoteContent;
    }
}
