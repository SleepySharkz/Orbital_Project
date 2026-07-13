package com.mindmesh.backend.dto.responses.marketplace;

import java.time.Instant;

import com.mindmesh.backend.enums.MarketplaceListingStatus;

public class MarketplaceListingPublishResponseDto {

  private final Long listingId;
  private final MarketplaceListingStatus status;
  private final String publicTitle;
  private final Integer entryCount;
  private final Instant publishedAt;

  public MarketplaceListingPublishResponseDto(
      Long listingId,
      MarketplaceListingStatus status,
      String publicTitle,
      Integer entryCount,
      Instant publishedAt) {
    this.listingId = listingId;
    this.status = status;
    this.publicTitle = publicTitle;
    this.entryCount = entryCount;
    this.publishedAt = publishedAt;
  }

  public Long getListingId() {
    return listingId;
  }

  public MarketplaceListingStatus getStatus() {
    return status;
  }

  public String getPublicTitle() {
    return publicTitle;
  }

  public Integer getEntryCount() {
    return entryCount;
  }

  public Instant getPublishedAt() {
    return publishedAt;
  }
}
