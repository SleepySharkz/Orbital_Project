package com.mindmesh.backend.dto.requests.marketplace;

import com.mindmesh.backend.enums.MarketplaceReportReason;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateMarketplaceReportRequestDto {

  @NotNull(message = "Report reason is required.")
  private MarketplaceReportReason reason;

  @Size(max = 1000, message = "Report details must not exceed 1000 characters.")
  private String details;

  public CreateMarketplaceReportRequestDto() {
  }

  public MarketplaceReportReason getReason() {
    return reason;
  }

  public void setReason(MarketplaceReportReason reason) {
    this.reason = reason;
  }

  public String getDetails() {
    return details;
  }

  public void setDetails(String details) {
    this.details = details;
  }
}
