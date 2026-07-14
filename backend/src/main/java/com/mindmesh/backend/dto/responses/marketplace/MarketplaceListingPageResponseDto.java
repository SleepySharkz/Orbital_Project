package com.mindmesh.backend.dto.responses.marketplace;

import java.util.List;

public class MarketplaceListingPageResponseDto {

  private final List<MarketplaceListingSummaryDto> items;
  private final Integer page;
  private final Integer size;
  private final Long totalItems;
  private final Integer totalPages;
  private final Boolean hasNext;

  public MarketplaceListingPageResponseDto(
      List<MarketplaceListingSummaryDto> items,
      Integer page,
      Integer size,
      Long totalItems,
      Integer totalPages,
      Boolean hasNext) {
    this.items = items;
    this.page = page;
    this.size = size;
    this.totalItems = totalItems;
    this.totalPages = totalPages;
    this.hasNext = hasNext;
  }

  public List<MarketplaceListingSummaryDto> getItems() {
    return items;
  }

  public Integer getPage() {
    return page;
  }

  public Integer getSize() {
    return size;
  }

  public Long getTotalItems() {
    return totalItems;
  }

  public Integer getTotalPages() {
    return totalPages;
  }

  public Boolean getHasNext() {
    return hasNext;
  }
}
