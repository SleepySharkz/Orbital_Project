package com.mindmesh.backend.dto.responses.marketplace;

import java.time.Instant;
import java.util.List;

public class MarketplaceImportDetailDto {

  private final Long id;
  private final Long sourceListingId;
  private final String sourceListingTitle;
  private final String sourcePublisherDisplayName;
  private final String courseCode;
  private final String schoolSem;
  private final String topic;
  private final Integer entryCount;
  private final Instant importedAt;
  private final List<MarketplaceImportedEntryDto> entries;

  public MarketplaceImportDetailDto(
      Long id,
      Long sourceListingId,
      String sourceListingTitle,
      String sourcePublisherDisplayName,
      String courseCode,
      String schoolSem,
      String topic,
      Integer entryCount,
      Instant importedAt,
      List<MarketplaceImportedEntryDto> entries) {
    this.id = id;
    this.sourceListingId = sourceListingId;
    this.sourceListingTitle = sourceListingTitle;
    this.sourcePublisherDisplayName = sourcePublisherDisplayName;
    this.courseCode = courseCode;
    this.schoolSem = schoolSem;
    this.topic = topic;
    this.entryCount = entryCount;
    this.importedAt = importedAt;
    this.entries = entries;
  }

  public Long getId() {
    return id;
  }

  public Long getSourceListingId() {
    return sourceListingId;
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

  public List<MarketplaceImportedEntryDto> getEntries() {
    return entries;
  }
}
