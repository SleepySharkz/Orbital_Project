package com.mindmesh.backend.dto.ai;

public class AIGeneratedCFCEntry {
    private Long requestItemId;
    private String flashcardQuestion;
    private String flashcardNoteContent;

    public AIGeneratedCFCEntry() {
    }

    public AIGeneratedCFCEntry(
        Long requestItemId,
        String flashcardQuestion,
        String flashcardNoteContent) {
            this.requestItemId = requestItemId;
            this.flashcardQuestion = flashcardQuestion;
            this.flashcardNoteContent = flashcardNoteContent;
        }
    
    //Getters
    public Long getRequestItemId() {
        return this.requestItemId;
    }

    public String getFlashcardQuestion() {
        return this.flashcardQuestion;
    }

    public String getFlashcardNoteContent() {
        return this.flashcardNoteContent;
    }

    //Setters
    public void setRequestItemId(Long requestItemId) {
        this.requestItemId = requestItemId;
    }

    public void setFlashcardQuestion(String newFlashcardQuestion) {
        this.flashcardQuestion = newFlashcardQuestion;
    }

    public void setFlashcardNoteContent(String newFlashcardNoteContent) {
        this.flashcardNoteContent = newFlashcardNoteContent;
    }
}
