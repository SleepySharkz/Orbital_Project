package com.mindmesh.backend.dto.responses.marketplace;

public class MarketplaceListingEntrySnapshotDto {

  private final Long id;
  private final String flashcardQuestion;
  private final String flashcardNoteContent;
  private final Integer displayOrder;

  public MarketplaceListingEntrySnapshotDto(
      Long id,
      String flashcardQuestion,
      String flashcardNoteContent,
      Integer displayOrder) {
    this.id = id;
    this.flashcardQuestion = flashcardQuestion;
    this.flashcardNoteContent = flashcardNoteContent;
    this.displayOrder = displayOrder;
  }

  public Long getId() {
    return id;
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
}
