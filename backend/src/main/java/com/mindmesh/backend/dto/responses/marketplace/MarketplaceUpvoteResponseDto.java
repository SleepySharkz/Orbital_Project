package com.mindmesh.backend.dto.responses.marketplace;

public class MarketplaceUpvoteResponseDto {

  private final Long listingId;
  private final Integer upvoteCount;
  private final boolean hasCurrentUserUpvoted;

  public MarketplaceUpvoteResponseDto(
      Long listingId,
      Integer upvoteCount,
      boolean hasCurrentUserUpvoted) {
    this.listingId = listingId;
    this.upvoteCount = upvoteCount;
    this.hasCurrentUserUpvoted = hasCurrentUserUpvoted;
  }

  public Long getListingId() {
    return listingId;
  }

  public Integer getUpvoteCount() {
    return upvoteCount;
  }

  public boolean isHasCurrentUserUpvoted() {
    return hasCurrentUserUpvoted;
  }
}
