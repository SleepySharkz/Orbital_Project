package com.mindmesh.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mindmesh.backend.dto.requests.marketplace.PublishMarketplaceListingRequestDto;
import com.mindmesh.backend.dto.responses.marketplace.MarketplaceListingPublishResponseDto;
import com.mindmesh.backend.security.CustomUserDetails;
import com.mindmesh.backend.service.marketplace.MarketplacePublishingService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/marketplace")
public class MarketplaceController {

    private final MarketplacePublishingService marketplacePublishingService;

    public MarketplaceController(MarketplacePublishingService marketplacePublishingService) {
        this.marketplacePublishingService = marketplacePublishingService;
    }

    @PostMapping("/listings")
    public ResponseEntity<MarketplaceListingPublishResponseDto> publishListing(
        @Valid @RequestBody PublishMarketplaceListingRequestDto request,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        MarketplaceListingPublishResponseDto response = marketplacePublishingService.publishTC(
            request,
            userDetails.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
