package com.mindmesh.backend.dto.responses.marketplace;

import java.time.Instant;
import java.util.List;

import com.mindmesh.backend.enums.PublisherVisibility;

public class MarketplaceListingDetailDto {

  private final Long id;
  private final String publicTitle;
  private final String description;
  private final String courseCode;
  private final String schoolSem;
  private final String topic;
  private final List<String> tags;
  private final String institution;
  private final PublisherVisibility publisherVisibility;
  private final String publisherDisplayName;
  private final Integer entryCount;
  private final Integer upvoteCount;
  private final boolean hasCurrentUserUpvoted;
  private final boolean hasCurrentUserReported;
  private final Integer importCount;
  private final Instant publishedAt;
  private final Instant updatedAt;
  private final List<MarketplaceListingEntrySnapshotDto> entries;

  public MarketplaceListingDetailDto(
      Long id,
      String publicTitle,
      String description,
      String courseCode,
      String schoolSem,
      String topic,
      List<String> tags,
      String institution,
      PublisherVisibility publisherVisibility,
      String publisherDisplayName,
      Integer entryCount,
      Integer upvoteCount,
      Integer importCount,
      Instant publishedAt,
      Instant updatedAt,
      List<MarketplaceListingEntrySnapshotDto> entries) {
    this(
        id, publicTitle, description, courseCode, schoolSem, topic, tags, institution,
        publisherVisibility, publisherDisplayName, entryCount, upvoteCount, false,
        false, importCount, publishedAt, updatedAt, entries);
  }

  public MarketplaceListingDetailDto(
      Long id,
      String publicTitle,
      String description,
      String courseCode,
      String schoolSem,
      String topic,
      List<String> tags,
      String institution,
      PublisherVisibility publisherVisibility,
      String publisherDisplayName,
      Integer entryCount,
      Integer upvoteCount,
      boolean hasCurrentUserUpvoted,
      boolean hasCurrentUserReported,
      Integer importCount,
      Instant publishedAt,
      Instant updatedAt,
      List<MarketplaceListingEntrySnapshotDto> entries) {
    this.id = id;
    this.publicTitle = publicTitle;
    this.description = description;
    this.courseCode = courseCode;
    this.schoolSem = schoolSem;
    this.topic = topic;
    this.tags = tags;
    this.institution = institution;
    this.publisherVisibility = publisherVisibility;
    this.publisherDisplayName = publisherDisplayName;
    this.entryCount = entryCount;
    this.upvoteCount = upvoteCount;
    this.hasCurrentUserUpvoted = hasCurrentUserUpvoted;
    this.hasCurrentUserReported = hasCurrentUserReported;
    this.importCount = importCount;
    this.publishedAt = publishedAt;
    this.updatedAt = updatedAt;
    this.entries = entries;
  }

  public Long getId() {
    return id;
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

  public List<String> getTags() {
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

  public Integer getEntryCount() {
    return entryCount;
  }

  public Integer getUpvoteCount() {
    return upvoteCount;
  }

  public boolean isHasCurrentUserUpvoted() {
    return hasCurrentUserUpvoted;
  }

  public boolean isHasCurrentUserReported() {
    return hasCurrentUserReported;
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

  public List<MarketplaceListingEntrySnapshotDto> getEntries() {
    return entries;
  }
}
