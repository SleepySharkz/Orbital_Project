package com.mindmesh.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class GeneratedCFCPage {

  @Column(name = "flashcard_question", columnDefinition = "TEXT")
  private String flashcardQuestion;

  @Column(name = "flashcard_note_content", columnDefinition = "TEXT")
  private String flashcardNoteContent;

  protected GeneratedCFCPage() {
  }

  public GeneratedCFCPage(
      String flashcardQuestion,
      String flashcardNoteContent) {

    this.flashcardQuestion = flashcardQuestion;
    this.flashcardNoteContent = flashcardNoteContent;
  }

  public String getFlashcardQuestion() {
    return flashcardQuestion;
  }

  public String getFlashcardNoteContent() {
    return flashcardNoteContent;
  }

  public void updateContent(
      String flashcardQuestion,
      String flashcardNoteContent) {

    this.flashcardQuestion = flashcardQuestion;
    this.flashcardNoteContent = flashcardNoteContent;
  }
}
