package com.mindmesh.backend.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

// Your import from the market place
@Entity
@Table(name = "marketplace_imports", uniqueConstraints = {
    @UniqueConstraint(name = "uk_marketplace_imports_importer_listing", columnNames = { "importer_id",
        "source_listing_id" })
}, indexes = {
    @Index(name = "idx_marketplace_imports_importer_imported_at", columnList = "importer_id,imported_at")
})
public class MarketplaceImport {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "importer_id", nullable = false)
  private User importer;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "source_listing_id", nullable = false)
  private MarketplaceListing sourceListing;

  @Column(name = "source_listing_title", nullable = false, length = 120)
  private String sourceListingTitle;

  @Column(name = "source_publisher_display_name", nullable = false, length = 120)
  private String sourcePublisherDisplayName;

  @Column(name = "course_code", nullable = false, length = 30)
  private String courseCode;

  @Column(name = "school_sem", nullable = false, length = 80)
  private String schoolSem;

  @Column(nullable = false, length = 120)
  private String topic;

  @CreationTimestamp
  @Column(name = "imported_at", nullable = false, updatable = false)
  private Instant importedAt;

  @OneToMany(mappedBy = "marketplaceImport", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("displayOrder ASC")
  private List<MarketplaceImportedEntry> entries = new ArrayList<>();

  protected MarketplaceImport() {
    // Required by JPA.
  }

  public MarketplaceImport(User importer, MarketplaceListing sourceListing) {
    if (importer == null || sourceListing == null) {
      throw new IllegalArgumentException("Importer and source listing are required.");
    }

    // Even though validation here seems redundant, just doing it out of abundance
    // of caution
    if (isBlank(sourceListing.getPublicTitle())
        || isBlank(sourceListing.getPublisherDisplayName())
        || isBlank(sourceListing.getCourseCode())
        || isBlank(sourceListing.getSchoolSem())
        || isBlank(sourceListing.getTopic())) {
      throw new IllegalArgumentException("Source listing metadata is required.");
    }

    this.importer = importer;
    this.sourceListing = sourceListing;
    this.sourceListingTitle = sourceListing.getPublicTitle();
    this.sourcePublisherDisplayName = sourceListing.getPublisherDisplayName();
    this.courseCode = sourceListing.getCourseCode();
    this.schoolSem = sourceListing.getSchoolSem();
    this.topic = sourceListing.getTopic();
  }

  public void addEntry(MarketplaceImportedEntry entry) {
    if (entry == null || entries.contains(entry)) {
      return;
    }

    if (entry.getMarketplaceImport() != null && entry.getMarketplaceImport() != this) {
      entry.getMarketplaceImport().removeEntry(entry);
    }

    entries.add(entry);
    entry.setMarketplaceImport(this);
  }

  public void removeEntry(MarketplaceImportedEntry entry) {
    if (entry == null) {
      return;
    }

    if (entries.remove(entry) && entry.getMarketplaceImport() == this) {
      entry.setMarketplaceImport(null);
    }
  }

  private boolean isBlank(String value) {
    return value == null || value.isBlank();
  }

  public Long getId() {
    return id;
  }

  public User getImporter() {
    return importer;
  }

  public MarketplaceListing getSourceListing() {
    return sourceListing;
  }

  public String getSourceListingTitle() {
    return sourceListingTitle;
  }

  public String getSourcePublisherDisplayName() {
    return sourcePublisherDisplayName;
  }

  public String getCourseCode() {
    return courseCode;
  }

  public String getSchoolSem() {
    return schoolSem;
  }

  public String getTopic() {
    return topic;
  }

  public Instant getImportedAt() {
    return importedAt;
  }

  public List<MarketplaceImportedEntry> getEntries() {
    return entries;
  }
}
