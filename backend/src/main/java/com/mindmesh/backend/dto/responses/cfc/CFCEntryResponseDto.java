package com.mindmesh.backend.dto.responses.cfc;

public class CFCEntryResponseDto {

  private Long id;
  private Long requestItemId;
  private String topic;
  private String flashcardQuestion;
  private String flashcardNoteContent;
  private SourceMaterialDto sourceMaterial;

  public CFCEntryResponseDto(
      Long id,
      Long requestItemId,
      String topic,
      String flashcardQuestion,
      String flashcardNoteContent,
      SourceMaterialDto sourceMaterial) {
    this.id = id;
    this.requestItemId = requestItemId;
    this.topic = topic;
    this.flashcardQuestion = flashcardQuestion;
    this.flashcardNoteContent = flashcardNoteContent;
    this.sourceMaterial = sourceMaterial;
  }

  public Long getId() {
    return id;
  }

  public Long getRequestItemId() {
    return requestItemId;
  }

  public String getTopic() {
    return topic;
  }

  public String getFlashcardQuestion() {
    return flashcardQuestion;
  }

  public String getFlashcardNoteContent() {
    return flashcardNoteContent;
  }

  public SourceMaterialDto getSourceMaterial() {
    return sourceMaterial;
  }
}
