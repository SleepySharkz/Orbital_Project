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
    name = "marketplace_listing_entry_snapshots",
    indexes = {
        @Index(
            name = "idx_marketplace_entry_snapshots_listing_id",
            columnList = "listing_id"
        )
    }
)
public class MarketplaceListingEntrySnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "listing_id", nullable = false)
    private MarketplaceListing listing;

    @Column(name = "source_entry_id", nullable = false)
    private Long sourceEntryId;

    @Column(name = "flashcard_question", nullable = false, columnDefinition = "TEXT")
    private String flashcardQuestion;

    @Column(name = "flashcard_note_content", nullable = false, columnDefinition = "TEXT")
    private String flashcardNoteContent;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(name = "source_entry_created_at")
    private LocalDateTime sourceEntryCreatedAt;

    protected MarketplaceListingEntrySnapshot() {
        // Required by JPA.
    }

    public MarketplaceListingEntrySnapshot(
        MarketplaceListing listing,
        Long sourceEntryId,
        String flashcardQuestion,
        String flashcardNoteContent,
        Integer displayOrder,
        LocalDateTime sourceEntryCreatedAt
    ) {
        if (listing == null) {
            throw new IllegalArgumentException("Marketplace listing is required.");
        }

        if (sourceEntryId == null) {
            throw new IllegalArgumentException("Source entry ID is required.");
        }

        if (isBlank(flashcardQuestion) || isBlank(flashcardNoteContent)) {
            throw new IllegalArgumentException("Published snapshot content is required.");
        }

        if (displayOrder == null || displayOrder < 0) {
            throw new IllegalArgumentException("Display order must be non-negative.");
        }

        this.listing = listing;
        this.sourceEntryId = sourceEntryId;
        this.flashcardQuestion = flashcardQuestion;
        this.flashcardNoteContent = flashcardNoteContent;
        this.displayOrder = displayOrder;
        this.sourceEntryCreatedAt = sourceEntryCreatedAt;

        listing.addEntry(this);
    }

    void setListing(MarketplaceListing listing) {
        this.listing = listing;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public Long getId() {
        return id;
    }

    public MarketplaceListing getListing() {
        return listing;
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

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public LocalDateTime getSourceEntryCreatedAt() {
        return sourceEntryCreatedAt;
    }
}
