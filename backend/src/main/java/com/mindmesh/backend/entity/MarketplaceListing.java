package com.mindmesh.backend.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.mindmesh.backend.enums.MarketplaceListingStatus;
import com.mindmesh.backend.enums.PublisherVisibility;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "marketplace_listings", indexes = {
    // We plan to use these columns to search through things, so need indexing to
    // speed up reads
    @Index(name = "idx_marketplace_listings_status_published_at", columnList = "status,published_at"),
    @Index(name = "idx_marketplace_listings_publisher_status", columnList = "publisher_id,status"),
    @Index(name = "idx_marketplace_listings_source_tc", columnList = "source_tc_id"),
    @Index(name = "idx_marketplace_listings_course_topic", columnList = "course_code,topic")
})
public class MarketplaceListing {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "publisher_id", nullable = false)
  private User publisher;

  @Column(name = "source_tc_id", nullable = false)
  private Long sourceTcId;

  @Column(name = "source_module_id", nullable = false)
  private Long sourceModuleId;

  @Column(name = "public_title", nullable = false, length = 120)
  private String publicTitle;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(name = "course_code", nullable = false, length = 30)
  private String courseCode;

  @Column(name = "school_sem", nullable = false, length = 80)
  private String schoolSem;

  @Column(nullable = false, length = 120)
  private String topic;

  // These fields can be left as blank
  @Column(length = 400)
  private String tags;

  @Column(length = 120)
  private String institution;

  // Give user the authority to decide if he wants the publisher to be known to
  // the public
  @Enumerated(EnumType.STRING)
  @Column(name = "publisher_visibility", nullable = false, length = 30)
  private PublisherVisibility publisherVisibility;

  @Column(name = "publisher_display_name", nullable = false, length = 120)
  private String publisherDisplayName;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private MarketplaceListingStatus status;

  @Column(name = "entry_count", nullable = false)
  private Integer entryCount;

  @Column(name = "upvote_count", nullable = false)
  private Integer upvoteCount;

  @Column(name = "import_count", nullable = false)
  private Integer importCount;

  @CreationTimestamp
  @Column(name = "published_at", nullable = false, updatable = false)
  private Instant publishedAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Column(name = "unlisted_at")
  private Instant unlistedAt;

  @Column(name = "removed_at")
  private Instant removedAt;

  // Gotta ensure any parent cascade operations are also made to the children
  @OneToMany(mappedBy = "listing", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<MarketplaceListingEntrySnapshot> entries = new ArrayList<>();

  protected MarketplaceListing() {
  }

  public MarketplaceListing(
      User publisher,
      Long sourceTcId,
      Long sourceModuleId,
      String publicTitle,
      String description,
      String courseCode,
      String schoolSem,
      String topic,
      String tags,
      String institution,
      PublisherVisibility publisherVisibility,
      String publisherDisplayName) {
    if (publisher == null) {
      throw new IllegalArgumentException("Publisher is required.");
    }

    if (sourceTcId == null || sourceModuleId == null) {
      throw new IllegalArgumentException("Source TC and module IDs are required.");
    }

    if (isBlank(publicTitle) || isBlank(courseCode) || isBlank(schoolSem) || isBlank(topic)) {
      throw new IllegalArgumentException("Listing metadata is required.");
    }

    if (publisherVisibility == null) {
      throw new IllegalArgumentException("Publisher visibility is required.");
    }

    if (isBlank(publisherDisplayName)) {
      throw new IllegalArgumentException("Publisher display name is required.");
    }

    this.publisher = publisher;
    this.sourceTcId = sourceTcId;
    this.sourceModuleId = sourceModuleId;
    this.publicTitle = publicTitle;
    this.description = description;
    this.courseCode = courseCode;
    this.schoolSem = schoolSem;
    this.topic = topic;
    this.tags = tags;
    this.institution = institution;
    this.publisherVisibility = publisherVisibility;
    this.publisherDisplayName = publisherDisplayName;
    this.status = MarketplaceListingStatus.PUBLISHED;
    this.entryCount = 0;
    this.upvoteCount = 0;
    this.importCount = 0;
  }

  public void addEntry(MarketplaceListingEntrySnapshot entry) {
    if (entry == null || entries.contains(entry)) {
      return;
    }

    if (entry.getListing() != null && entry.getListing() != this) {
      entry.getListing().removeEntry(entry);
    }

    entries.add(entry);
    entry.setListing(this);
    entryCount = entries.size();
  }

  public void removeEntry(MarketplaceListingEntrySnapshot entry) {
    if (entry == null) {
      return;
    }

    if (entries.remove(entry) && entry.getListing() == this) {
      entry.setListing(null);
      entryCount = entries.size();
    }
  }

  public void unlist(Instant unlistedAt) {
    if (unlistedAt == null) {
      throw new IllegalArgumentException("Unlisted timestamp is required.");
    }

    this.status = MarketplaceListingStatus.UNLISTED;
    this.unlistedAt = unlistedAt;
  }

  public void markUnderReview() {
    this.status = MarketplaceListingStatus.UNDER_REVIEW;
  }

  public void remove(Instant removedAt) {
    if (removedAt == null) {
      throw new IllegalArgumentException("Removed timestamp is required.");
    }

    this.status = MarketplaceListingStatus.REMOVED;
    this.removedAt = removedAt;
  }

  private boolean isBlank(String value) {
    return value == null || value.isBlank();
  }

  public Long getId() {
    return id;
  }

  public User getPublisher() {
    return publisher;
  }

  public Long getSourceTcId() {
    return sourceTcId;
  }

  public Long getSourceModuleId() {
    return sourceModuleId;
  }

  public String getPublicTitle() {
    return publicTitle;
  }

  public String getDescription() {
    return description;
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

  public String getTags() {
    return tags;
  }

  public String getInstitution() {
    return institution;
  }

  public PublisherVisibility getPublisherVisibility() {
    return publisherVisibility;
  }

  public String getPublisherDisplayName() {
    return publisherDisplayName;
  }

  public MarketplaceListingStatus getStatus() {
    return status;
  }

  public Integer getEntryCount() {
    return entryCount;
  }

  public Integer getUpvoteCount() {
    return upvoteCount;
  }

  public Integer getImportCount() {
    return importCount;
  }

  public Instant getPublishedAt() {
    return publishedAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public Instant getUnlistedAt() {
    return unlistedAt;
  }

  public Instant getRemovedAt() {
    return removedAt;
  }

  public List<MarketplaceListingEntrySnapshot> getEntries() {
    return entries;
  }
}
