package com.mindmesh.backend.dto.responses.marketplace;

import java.time.Instant;

public class MarketplaceImportResponseDto {

  private final Long importId;
  private final Long sourceListingId;
  private final String sourceListingTitle;
  private final Integer entryCount;
  private final Instant importedAt;

  public MarketplaceImportResponseDto(
      Long importId,
      Long sourceListingId,
      String sourceListingTitle,
      Integer entryCount,
      Instant importedAt) {
    this.importId = importId;
    this.sourceListingId = sourceListingId;
    this.sourceListingTitle = sourceListingTitle;
    this.entryCount = entryCount;
    this.importedAt = importedAt;
  }

  public Long getImportId() {
    return importId;
  }

  public Long getSourceListingId() {
    return sourceListingId;
  }

  public String getSourceListingTitle() {
    return sourceListingTitle;
  }

  public Integer getEntryCount() {
    return entryCount;
  }

  public Instant getImportedAt() {
    return importedAt;
  }
}
