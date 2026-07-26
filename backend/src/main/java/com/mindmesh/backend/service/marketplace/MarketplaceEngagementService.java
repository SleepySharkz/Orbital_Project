package com.mindmesh.backend.service.marketplace;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.mindmesh.backend.dto.requests.marketplace.CreateMarketplaceReportRequestDto;
import com.mindmesh.backend.dto.responses.marketplace.MarketplaceReportResponseDto;
import com.mindmesh.backend.dto.responses.marketplace.MarketplaceUpvoteResponseDto;
import com.mindmesh.backend.entity.MarketplaceListing;
import com.mindmesh.backend.entity.MarketplaceListingReport;
import com.mindmesh.backend.entity.User;
import com.mindmesh.backend.enums.MarketplaceListingStatus;
import com.mindmesh.backend.repository.MarketplaceListingReportRepository;
import com.mindmesh.backend.repository.MarketplaceListingRepository;
import com.mindmesh.backend.repository.UserRepository;

@Service
public class MarketplaceEngagementService {

  private static final long UNDER_REVIEW_REPORT_THRESHOLD = 3;

  private final MarketplaceListingRepository marketplaceListingRepository;
  private final MarketplaceListingReportRepository marketplaceListingReportRepository;
  private final UserRepository userRepository;

  public MarketplaceEngagementService(
      MarketplaceListingRepository marketplaceListingRepository,
      MarketplaceListingReportRepository marketplaceListingReportRepository,
      UserRepository userRepository) {
    this.marketplaceListingRepository = marketplaceListingRepository;
    this.marketplaceListingReportRepository = marketplaceListingReportRepository;
    this.userRepository = userRepository;
  }

  @Transactional
  public MarketplaceUpvoteResponseDto addUpvote(Long listingId, Long userId) {
    MarketplaceListing listing = getPublishedListing(listingId);
    User user = getUser(userId);
    listing.addUpvote(user);
    marketplaceListingRepository.save(listing);

    return toUpvoteResponse(listing, user);
  }

  @Transactional
  public MarketplaceUpvoteResponseDto removeUpvote(Long listingId, Long userId) {
    MarketplaceListing listing = getPublishedListing(listingId);
    User user = getUser(userId);
    listing.removeUpvote(user);
    marketplaceListingRepository.save(listing);

    return toUpvoteResponse(listing, user);
  }

  @Transactional
  public MarketplaceReportResponseDto reportListing(
      Long listingId,
      Long reporterId,
      CreateMarketplaceReportRequestDto request) {
    MarketplaceListing listing = getPublishedListing(listingId);

    if (marketplaceListingReportRepository.existsByListingIdAndReporterId(listingId, reporterId)) {
      throw duplicateReport();
    }

    User reporter = getUser(reporterId);
    MarketplaceListingReport report = new MarketplaceListingReport(
        listing,
        reporter,
        request.getReason(),
        request.getDetails());

    MarketplaceListingReport savedReport;
    try {
      savedReport = marketplaceListingReportRepository.saveAndFlush(report);
    } catch (DataIntegrityViolationException exception) {
      throw duplicateReport();
    }

    long reportCount = marketplaceListingReportRepository.countByListingId(listingId);
    if (reportCount >= UNDER_REVIEW_REPORT_THRESHOLD) {
      listing.markUnderReview();
      marketplaceListingRepository.save(listing);
    }

    return new MarketplaceReportResponseDto(
        listing.getId(),
        savedReport.getId(),
        listing.getStatus());
  }

  private MarketplaceListing getPublishedListing(Long listingId) {
    return marketplaceListingRepository
        .findByIdAndStatus(listingId, MarketplaceListingStatus.PUBLISHED)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Marketplace listing not found."));
  }

  private User getUser(Long userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));
  }

  private MarketplaceUpvoteResponseDto toUpvoteResponse(MarketplaceListing listing, User user) {
    return new MarketplaceUpvoteResponseDto(
        listing.getId(),
        listing.getUpvoteCount(),
        listing.hasUpvoteFrom(user));
  }

  private ResponseStatusException duplicateReport() {
    return new ResponseStatusException(
        HttpStatus.CONFLICT,
        "You have already reported this marketplace listing.");
  }
}
