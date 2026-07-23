package com.mindmesh.backend.dto.responses.marketplace;

import java.time.Instant;

public class MarketplaceImportSummaryDto {

  private final Long id;
  private final String sourceListingTitle;
  private final String sourcePublisherDisplayName;
  private final String courseCode;
  private final String schoolSem;
  private final String topic;
  private final Integer entryCount;
  private final Instant importedAt;

  public MarketplaceImportSummaryDto(
      Long id,
      String sourceListingTitle,
      String sourcePublisherDisplayName,
      String courseCode,
      String schoolSem,
      String topic,
      Integer entryCount,
      Instant importedAt) {
    this.id = id;
    this.sourceListingTitle = sourceListingTitle;
    this.sourcePublisherDisplayName = sourcePublisherDisplayName;
    this.courseCode = courseCode;
    this.schoolSem = schoolSem;
    this.topic = topic;
    this.entryCount = entryCount;
    this.importedAt = importedAt;
  }

  public Long getId() {
    return id;
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

  public Integer getEntryCount() {
    return entryCount;
  }

  public Instant getImportedAt() {
    return importedAt;
  }
}
