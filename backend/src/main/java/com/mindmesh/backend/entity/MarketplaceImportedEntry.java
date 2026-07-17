package com.mindmesh.backend.entity;

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
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "marketplace_imported_entries", uniqueConstraints = {
    @UniqueConstraint(name = "uk_marketplace_imported_entries_source", columnNames = { "marketplace_import_id",
        "source_listing_entry_id" })
}, indexes = {
    @Index(name = "idx_marketplace_imported_entries_import", columnList = "marketplace_import_id")
})
public class MarketplaceImportedEntry {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "marketplace_import_id", nullable = false)
  private MarketplaceImport marketplaceImport;

  @Column(name = "source_listing_entry_id", nullable = false)
  private Long sourceListingEntryId;

  @Column(name = "flashcard_question", nullable = false, columnDefinition = "TEXT")
  private String flashcardQuestion;

  @Column(name = "flashcard_note_content", nullable = false, columnDefinition = "TEXT")
  private String flashcardNoteContent;

  @Column(name = "display_order", nullable = false)
  private Integer displayOrder;

  protected MarketplaceImportedEntry() {
    // Required by JPA.
  }

  public MarketplaceImportedEntry(
      MarketplaceImport marketplaceImport,
      Long sourceListingEntryId,
      String flashcardQuestion,
      String flashcardNoteContent,
      Integer displayOrder) {
    if (marketplaceImport == null) {
      throw new IllegalArgumentException("Marketplace import is required.");
    }

    if (sourceListingEntryId == null) {
      throw new IllegalArgumentException("Source listing entry ID is required.");
    }

    if (isBlank(flashcardQuestion) || isBlank(flashcardNoteContent)) {
      throw new IllegalArgumentException("Imported entry content is required.");
    }

    if (displayOrder == null || displayOrder < 0) {
      throw new IllegalArgumentException("Display order must be non-negative.");
    }

    this.marketplaceImport = marketplaceImport;
    this.sourceListingEntryId = sourceListingEntryId;
    this.flashcardQuestion = flashcardQuestion;
    this.flashcardNoteContent = flashcardNoteContent;
    this.displayOrder = displayOrder;

    // Constructor must self inert itself (Done for bidrectional relationships)
    marketplaceImport.addEntry(this);
  }

  void setMarketplaceImport(MarketplaceImport marketplaceImport) {
    this.marketplaceImport = marketplaceImport;
  }

  private boolean isBlank(String value) {
    return value == null || value.isBlank();
  }

  public Long getId() {
    return id;
  }

  public MarketplaceImport getMarketplaceImport() {
    return marketplaceImport;
  }

  public Long getSourceListingEntryId() {
    return sourceListingEntryId;
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
