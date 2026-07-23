package com.mindmesh.backend.dto.responses.marketplace;

import java.util.List;

public class MarketplaceListingManagementPageResponseDto {

  private final List<MarketplaceListingManagementSummaryDto> items;
  private final Integer page;
  private final Integer size;
  private final Long totalItems;
  private final Integer totalPages;
  private final Boolean hasNext;

  // Support for pagination just like normal page response dto
  public MarketplaceListingManagementPageResponseDto(
      List<MarketplaceListingManagementSummaryDto> items,
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

  public List<MarketplaceListingManagementSummaryDto> getItems() {
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
