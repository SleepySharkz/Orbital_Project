package com.mindmesh.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mindmesh.backend.dto.requests.marketplace.PublishMarketplaceListingRequestDto;
import com.mindmesh.backend.dto.requests.marketplace.UpdateMarketplaceListingMetadataRequestDto;
import com.mindmesh.backend.dto.responses.marketplace.MarketplaceImportDetailDto;
import com.mindmesh.backend.dto.responses.marketplace.MarketplaceImportResponseDto;
import com.mindmesh.backend.dto.responses.marketplace.MarketplaceImportSummaryDto;
import com.mindmesh.backend.dto.responses.marketplace.MarketplaceListingDetailDto;
import com.mindmesh.backend.dto.responses.marketplace.MarketplaceListingManagementDetailDto;
import com.mindmesh.backend.dto.responses.marketplace.MarketplaceListingManagementPageResponseDto;
import com.mindmesh.backend.dto.responses.marketplace.MarketplaceListingPageResponseDto;
import com.mindmesh.backend.dto.responses.marketplace.MarketplaceListingPublishResponseDto;
import com.mindmesh.backend.security.CustomUserDetails;
import com.mindmesh.backend.service.marketplace.MarketplaceBrowsingService;
import com.mindmesh.backend.service.marketplace.MarketplaceImportService;
import com.mindmesh.backend.service.marketplace.MarketplaceListingManagementService;
import com.mindmesh.backend.service.marketplace.MarketplacePublishingService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/marketplace")
public class MarketplaceController {

  private final MarketplacePublishingService marketplacePublishingService;
  private final MarketplaceBrowsingService marketplaceBrowsingService;
  private final MarketplaceImportService marketplaceImportService;
  private final MarketplaceListingManagementService marketplaceListingManagementService;

  public MarketplaceController(
      MarketplacePublishingService marketplacePublishingService,
      MarketplaceBrowsingService marketplaceBrowsingService,
      MarketplaceImportService marketplaceImportService,
      MarketplaceListingManagementService marketplaceListingManagementService) {
    this.marketplacePublishingService = marketplacePublishingService;
    this.marketplaceBrowsingService = marketplaceBrowsingService;
    this.marketplaceImportService = marketplaceImportService;
    this.marketplaceListingManagementService = marketplaceListingManagementService;
  }

  // Separate listings for public browsing and publising
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
      // Search request parameters and filters
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

  @PostMapping("/listings/{listingId}/import")
  public ResponseEntity<MarketplaceImportResponseDto> importListing(
      @PathVariable Long listingId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    MarketplaceImportResponseDto response = marketplaceImportService.importListing(
        listingId,
        userDetails.getId());

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("/imports")
  public ResponseEntity<List<MarketplaceImportSummaryDto>> listImports(
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    List<MarketplaceImportSummaryDto> response = marketplaceImportService.listImports(userDetails.getId());

    return ResponseEntity.ok(response);
  }

  @GetMapping("/imports/{importId}")
  public ResponseEntity<MarketplaceImportDetailDto> getImportDetail(
      @PathVariable Long importId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    MarketplaceImportDetailDto response = marketplaceImportService.getImportDetail(
        importId,
        userDetails.getId());

    return ResponseEntity.ok(response);
  }

  // Separate my listings endpoints for management
  @GetMapping("/my-listings")
  public ResponseEntity<MarketplaceListingManagementPageResponseDto> listMyListings(
      @RequestParam(required = false, defaultValue = "0") Integer page,
      @RequestParam(required = false, defaultValue = "12") Integer size,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    MarketplaceListingManagementPageResponseDto response = marketplaceListingManagementService.listMyListings(
        userDetails.getId(),
        page,
        size);

    return ResponseEntity.ok(response);
  }

  @GetMapping("/my-listings/{listingId}")
  public ResponseEntity<MarketplaceListingManagementDetailDto> getMyListingDetail(
      @PathVariable Long listingId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    MarketplaceListingManagementDetailDto response = marketplaceListingManagementService.getMyListingDetail(
        listingId,
        userDetails.getId());

    return ResponseEntity.ok(response);
  }

  // First time using patch method, kinda nervous
  @PatchMapping("/my-listings/{listingId}")
  public ResponseEntity<MarketplaceListingManagementDetailDto> updateMyListingMetadata(
      @PathVariable Long listingId,
      @Valid @RequestBody UpdateMarketplaceListingMetadataRequestDto request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    MarketplaceListingManagementDetailDto response = marketplaceListingManagementService.updateMetadata(
        listingId,
        userDetails.getId(),
        request);

    return ResponseEntity.ok(response);
  }

  @PostMapping("/my-listings/{listingId}/unlist")
  public ResponseEntity<MarketplaceListingManagementDetailDto> unlistMyListing(
      @PathVariable Long listingId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    MarketplaceListingManagementDetailDto response = marketplaceListingManagementService.unlist(
        listingId,
        userDetails.getId());

    return ResponseEntity.ok(response);
  }

  @PostMapping("/my-listings/{listingId}/republish")
  public ResponseEntity<MarketplaceListingManagementDetailDto> republishMyListing(
      @PathVariable Long listingId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    MarketplaceListingManagementDetailDto response = marketplaceListingManagementService.republish(
        listingId,
        userDetails.getId());

    return ResponseEntity.ok(response);
  }
}
