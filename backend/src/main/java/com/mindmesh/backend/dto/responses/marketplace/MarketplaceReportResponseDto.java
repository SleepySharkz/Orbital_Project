package com.mindmesh.backend.dto.responses.marketplace;

import com.mindmesh.backend.enums.MarketplaceListingStatus;

public class MarketplaceReportResponseDto {

  private final Long listingId;
  private final Long reportId;
  private final MarketplaceListingStatus listingStatus;

  public MarketplaceReportResponseDto(
      Long listingId,
      Long reportId,
      MarketplaceListingStatus listingStatus) {
    this.listingId = listingId;
    this.reportId = reportId;
    this.listingStatus = listingStatus;
  }

  public Long getListingId() {
    return listingId;
  }

  public Long getReportId() {
    return reportId;
  }

  public MarketplaceListingStatus getListingStatus() {
    return listingStatus;
  }
}
