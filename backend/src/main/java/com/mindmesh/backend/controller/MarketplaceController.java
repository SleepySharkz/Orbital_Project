package com.mindmesh.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mindmesh.backend.dto.requests.marketplace.PublishMarketplaceListingRequestDto;
import com.mindmesh.backend.dto.responses.marketplace.MarketplaceListingDetailDto;
import com.mindmesh.backend.dto.responses.marketplace.MarketplaceListingPageResponseDto;
import com.mindmesh.backend.dto.responses.marketplace.MarketplaceListingPublishResponseDto;
import com.mindmesh.backend.security.CustomUserDetails;
import com.mindmesh.backend.service.marketplace.MarketplaceBrowsingService;
import com.mindmesh.backend.service.marketplace.MarketplacePublishingService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/marketplace")
public class MarketplaceController {

  private final MarketplacePublishingService marketplacePublishingService;
  private final MarketplaceBrowsingService marketplaceBrowsingService;

  public MarketplaceController(
      MarketplacePublishingService marketplacePublishingService,
      MarketplaceBrowsingService marketplaceBrowsingService) {
    this.marketplacePublishingService = marketplacePublishingService;
    this.marketplaceBrowsingService = marketplaceBrowsingService;
  }

  @PostMapping("/listings")
  public ResponseEntity<MarketplaceListingPublishResponseDto> publishListing(
      @Valid @RequestBody PublishMarketplaceListingRequestDto request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    MarketplaceListingPublishResponseDto response = marketplacePublishingService.publishTC(
        request,
        userDetails.getId());

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("/listings")
  public ResponseEntity<MarketplaceListingPageResponseDto> browseListings(
      // request parameters
      @RequestParam(required = false) String q,
      @RequestParam(required = false, name = "module") String module,
      @RequestParam(required = false) String topic,
      @RequestParam(required = false) String tag,
      @RequestParam(required = false, defaultValue = "newest") String sort,
      @RequestParam(required = false, defaultValue = "0") Integer page,
      @RequestParam(required = false, defaultValue = "12") Integer size) {
    MarketplaceListingPageResponseDto response = marketplaceBrowsingService.browseListings(
        q,
        module,
        topic,
        tag,
        sort,
        page,
        size);

    return ResponseEntity.ok(response);
  }

  @GetMapping("/listings/{listingId}")
  public ResponseEntity<MarketplaceListingDetailDto> getListingDetail(
      @PathVariable Long listingId) {
    MarketplaceListingDetailDto response = marketplaceBrowsingService.getPublishedListingDetail(listingId);

    return ResponseEntity.ok(response);
  }
}
