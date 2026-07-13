package com.mindmesh.backend.dto.responses.marketplace;

import java.time.Instant;
import java.util.List;

import com.mindmesh.backend.enums.PublisherVisibility;

public class MarketplaceListingSummaryDto {

  // All the details to be shown in the card
  // Less detailed since we dont gotta know allat
  private final Long id;
  private final String publicTitle;
  private final String descriptionPreview;
  private final String courseCode;
  private final String schoolSem;
  private final String topic;
  private final List<String> tags;
  private final PublisherVisibility publisherVisibility;
  private final String publisherDisplayName;
  private final Integer entryCount;
  private final Integer upvoteCount;
  private final Integer importCount;
  private final Instant publishedAt;

  public MarketplaceListingSummaryDto(
      Long id,
      String publicTitle,
      String descriptionPreview,
      String courseCode,
      String schoolSem,
      String topic,
      List<String> tags,
      PublisherVisibility publisherVisibility,
      String publisherDisplayName,
      Integer entryCount,
      Integer upvoteCount,
      Integer importCount,
      Instant publishedAt) {
    this.id = id;
    this.publicTitle = publicTitle;
    this.descriptionPreview = descriptionPreview;
    this.courseCode = courseCode;
    this.schoolSem = schoolSem;
    this.topic = topic;
    this.tags = tags;
    this.publisherVisibility = publisherVisibility;
    this.publisherDisplayName = publisherDisplayName;
    this.entryCount = entryCount;
    this.upvoteCount = upvoteCount;
    this.importCount = importCount;
    this.publishedAt = publishedAt;
  }

  public Long getId() {
    return id;
  }

  public String getPublicTitle() {
    return publicTitle;
  }

  public String getDescriptionPreview() {
    return descriptionPreview;
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

  public Integer getImportCount() {
    return importCount;
  }

  public Instant getPublishedAt() {
    return publishedAt;
  }
}
