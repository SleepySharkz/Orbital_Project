package com.mindmesh.backend.service.marketplace;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.mindmesh.backend.dto.responses.marketplace.MarketplaceListingDetailDto;
import com.mindmesh.backend.dto.responses.marketplace.MarketplaceListingEntrySnapshotDto;
import com.mindmesh.backend.dto.responses.marketplace.MarketplaceListingPageResponseDto;
import com.mindmesh.backend.dto.responses.marketplace.MarketplaceListingSummaryDto;
import com.mindmesh.backend.entity.MarketplaceListing;
import com.mindmesh.backend.entity.MarketplaceListingEntrySnapshot;
import com.mindmesh.backend.enums.MarketplaceListingStatus;
import com.mindmesh.backend.repository.MarketplaceListingReportRepository;
import com.mindmesh.backend.repository.MarketplaceListingRepository;

import jakarta.transaction.Transactional;

@Service
public class MarketplaceBrowsingService {

  private static final int DEFAULT_PAGE_SIZE = 12;
  private static final int MAX_PAGE_SIZE = 50;

  private final MarketplaceListingRepository marketplaceListingRepository;
  private final MarketplaceListingReportRepository marketplaceListingReportRepository;

  public MarketplaceBrowsingService(
      MarketplaceListingRepository marketplaceListingRepository,
      MarketplaceListingReportRepository marketplaceListingReportRepository) {
    this.marketplaceListingRepository = marketplaceListingRepository;
    this.marketplaceListingReportRepository = marketplaceListingReportRepository;
  }

  @Transactional
  public MarketplaceListingPageResponseDto browseListings(
      String q,
      String module,
      String topic,
      String tag,
      String sort,
      Integer page,
      Integer size) {
    int normalizedPage = normalizePage(page);
    int normalizedSize = normalizeSize(size);

    Page<MarketplaceListing> listingPage = marketplaceListingRepository.searchPublishedListings(
        MarketplaceListingStatus.PUBLISHED,
        normalizeOptional(q),
        normalizeOptional(module),
        normalizeOptional(topic),
        normalizeOptional(tag),
        PageRequest.of(normalizedPage, normalizedSize, sortFor(sort)));

    List<MarketplaceListingSummaryDto> items = listingPage.getContent()
        .stream()
        .map(this::toSummaryDto)
        .toList();

    // For frontend to render the listing page
    return new MarketplaceListingPageResponseDto(
        items,
        listingPage.getNumber(),
        listingPage.getSize(),
        listingPage.getTotalElements(),
        listingPage.getTotalPages(),
        listingPage.hasNext());
  }

  @Transactional
  public MarketplaceListingDetailDto getPublishedListingDetail(Long listingId, Long userId) {
    MarketplaceListing listing = marketplaceListingRepository
        .findByIdAndStatus(listingId, MarketplaceListingStatus.PUBLISHED)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Marketplace listing not found."));

    boolean hasCurrentUserUpvoted = marketplaceListingRepository.hasUpvoteFromUser(
        listingId,
        userId);
    boolean hasCurrentUserReported = marketplaceListingReportRepository
        .existsByListingIdAndReporterId(listingId, userId);
    return toDetailDto(listing, hasCurrentUserUpvoted, hasCurrentUserReported);
  }

  private MarketplaceListingSummaryDto toSummaryDto(MarketplaceListing listing) {
    return new MarketplaceListingSummaryDto(
        listing.getId(),
        listing.getPublicTitle(),
        descriptionPreview(listing.getDescription()),
        listing.getCourseCode(),
        listing.getSchoolSem(),
        listing.getTopic(),
        splitTags(listing.getTags()),
        listing.getPublisherVisibility(),
        listing.getPublisherDisplayName(),
        listing.getEntryCount(),
        listing.getUpvoteCount(),
        listing.getImportCount(),
        listing.getPublishedAt());
  }

  private MarketplaceListingDetailDto toDetailDto(
      MarketplaceListing listing,
      boolean hasCurrentUserUpvoted,
      boolean hasCurrentUserReported) {
    List<MarketplaceListingEntrySnapshotDto> entries = listing.getEntries()
        .stream()
        .sorted(Comparator.comparing(MarketplaceListingEntrySnapshot::getDisplayOrder))
        .map(this::toEntryDto)
        .toList();

    return new MarketplaceListingDetailDto(
        listing.getId(),
        listing.getPublicTitle(),
        listing.getDescription(),
        listing.getCourseCode(),
        listing.getSchoolSem(),
        listing.getTopic(),
        splitTags(listing.getTags()),
        listing.getInstitution(),
        listing.getPublisherVisibility(),
        listing.getPublisherDisplayName(),
        listing.getEntryCount(),
        listing.getUpvoteCount(),
        hasCurrentUserUpvoted,
        hasCurrentUserReported,
        listing.getImportCount(),
        listing.getPublishedAt(),
        listing.getUpdatedAt(),
        entries);
  }

  // Map to entry dto
  private MarketplaceListingEntrySnapshotDto toEntryDto(MarketplaceListingEntrySnapshot entry) {
    return new MarketplaceListingEntrySnapshotDto(
        entry.getId(),
        entry.getFlashcardQuestion(),
        entry.getFlashcardNoteContent(),
        entry.getDisplayOrder());
  }

  private Sort sortFor(String sort) {
    String normalizedSort = normalizeOptional(sort);

    if (normalizedSort == null || normalizedSort.equals("newest")) {
      return Sort.by(
          Sort.Order.desc("publishedAt"),
          Sort.Order.desc("id"));
    }

    if (normalizedSort.equals("mostUpvoted")) {
      return Sort.by(
          Sort.Order.desc("upvoteCount"),
          Sort.Order.desc("publishedAt"),
          Sort.Order.desc("id"));
    }

    if (normalizedSort.equals("mostImported")) {
      return Sort.by(
          Sort.Order.desc("importCount"),
          Sort.Order.desc("publishedAt"),
          Sort.Order.desc("id"));
    }

    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported marketplace sort option.");
  }

  // Important normalisation to catch bad requests and filter them
  private int normalizePage(Integer page) {
    if (page == null) {
      return 0;
    }

    if (page < 0) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Page must be zero or greater.");
    }

    return page;
  }

  private int normalizeSize(Integer size) {
    if (size == null) {
      return DEFAULT_PAGE_SIZE;
    }

    if (size < 1) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Page size must be at least 1.");
    }

    return Math.min(size, MAX_PAGE_SIZE);
  }

  // Trim all text for standardisation and comparison
  private String normalizeOptional(String value) {
    if (value == null) {
      return null;
    }

    String normalizedValue = value.trim();
    return normalizedValue.isEmpty() ? null : normalizedValue;
  }

  private List<String> splitTags(String tags) {
    if (tags == null || tags.isBlank()) {
      return List.of();
    }

    return Arrays.stream(tags.split(","))
        .map(String::trim)
        .filter(tag -> !tag.isEmpty())
        .toList();
  }

  private String descriptionPreview(String description) {
    String normalizedDescription = normalizeOptional(description);

    if (normalizedDescription == null) {
      return null;
    }

    if (normalizedDescription.length() <= 160) {
      return normalizedDescription;
    }

    return normalizedDescription.substring(0, 157) + "...";
  }
}
