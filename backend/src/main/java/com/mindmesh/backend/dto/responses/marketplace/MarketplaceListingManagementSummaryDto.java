package com.mindmesh.backend.dto.responses.marketplace;

import java.time.Instant;

import com.mindmesh.backend.enums.MarketplaceListingStatus;

public class MarketplaceListingManagementSummaryDto {

  private final Long id;
  private final String publicTitle;
  private final String courseCode;
  private final String schoolSem;
  private final String topic;
  private final MarketplaceListingStatus status;
  private final Integer entryCount;
  private final Integer upvoteCount;
  private final Integer importCount;
  private final Instant publishedAt;
  private final Instant updatedAt;
  private final Instant unlistedAt;

  // The non detail version of the management dto response
  public MarketplaceListingManagementSummaryDto(
      Long id,
      String publicTitle,
      String courseCode,
      String schoolSem,
      String topic,
      MarketplaceListingStatus status,
      Integer entryCount,
      Integer upvoteCount,
      Integer importCount,
      Instant publishedAt,
      Instant updatedAt,
      Instant unlistedAt) {
    this.id = id;
    this.publicTitle = publicTitle;
    this.courseCode = courseCode;
    this.schoolSem = schoolSem;
    this.topic = topic;
    this.status = status;
    this.entryCount = entryCount;
    this.upvoteCount = upvoteCount;
    this.importCount = importCount;
    this.publishedAt = publishedAt;
    this.updatedAt = updatedAt;
    this.unlistedAt = unlistedAt;
  }

  public Long getId() {
    return id;
  }

  public String getPublicTitle() {
    return publicTitle;
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
}
