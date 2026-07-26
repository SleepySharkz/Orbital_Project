package com.mindmesh.backend.service.marketplace;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.mindmesh.backend.dto.requests.marketplace.UpdateMarketplaceListingMetadataRequestDto;
import com.mindmesh.backend.dto.responses.marketplace.MarketplaceListingEntrySnapshotDto;
import com.mindmesh.backend.dto.responses.marketplace.MarketplaceListingManagementDetailDto;
import com.mindmesh.backend.dto.responses.marketplace.MarketplaceListingManagementPageResponseDto;
import com.mindmesh.backend.dto.responses.marketplace.MarketplaceListingManagementSummaryDto;
import com.mindmesh.backend.entity.CFCEntry;
import com.mindmesh.backend.entity.MarketplaceListing;
import com.mindmesh.backend.entity.MarketplaceListingEntrySnapshot;
import com.mindmesh.backend.entity.TC;
import com.mindmesh.backend.enums.MarketplaceListingStatus;
import com.mindmesh.backend.enums.PublisherVisibility;
import com.mindmesh.backend.repository.MarketplaceListingRepository;
import com.mindmesh.backend.repository.TCRepository;

import jakarta.transaction.Transactional;

@Service
public class MarketplaceListingManagementService {

  private static final int DEFAULT_PAGE_SIZE = 12;
  private static final int MAX_PAGE_SIZE = 50;

  private final MarketplaceListingRepository marketplaceListingRepository;
  private final TCRepository tcRepository;

  public MarketplaceListingManagementService(
      MarketplaceListingRepository marketplaceListingRepository,
      TCRepository tcRepository) {
    this.marketplaceListingRepository = marketplaceListingRepository;
    this.tcRepository = tcRepository;
  }

  // Return all of my listings
  @Transactional
  public MarketplaceListingManagementPageResponseDto listMyListings(Long publisherId, Integer page, Integer size) {
    Page<MarketplaceListing> listingPage = marketplaceListingRepository.findByPublisherIdOrderByUpdatedAtDesc(
        publisherId,
        PageRequest.of(
            // Recall that we normalize to safegaurd against bad requests
            normalizePage(page),
            normalizeSize(size),
            Sort.by(Sort.Order.desc("updatedAt"), Sort.Order.desc("id"))));

    List<MarketplaceListingManagementSummaryDto> items = listingPage.getContent() // getContent() returns a page of item
                                                                                  // that
                                                                                  // Page encapsulates
                                                                                  // (MarketplaceListing)
        .stream()
        .map(x -> this.toManagementSummaryDto(x))
        .toList();

    return new MarketplaceListingManagementPageResponseDto(
        items,
        listingPage.getNumber(),
        listingPage.getSize(),
        listingPage.getTotalElements(),
        listingPage.getTotalPages(),
        listingPage.hasNext());
  }

  @Transactional
  public MarketplaceListingManagementDetailDto getMyListingDetail(Long listingId, Long publisherId) {
    MarketplaceListing listing = getOwnedListingOrThrow(listingId, publisherId);

    return toManagementDetailDto(listing, publisherId);
  }

  @Transactional
  public MarketplaceListingManagementDetailDto updateMetadata(
      Long listingId,
      Long publisherId,
      UpdateMarketplaceListingMetadataRequestDto request) {
    MarketplaceListing listing = getOwnedListingOrThrow(listingId, publisherId);

    if (listing.getStatus() == MarketplaceListingStatus.REMOVED) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Removed listings cannot be edited.");
    }

    PublisherVisibility publisherVisibility = request.getPublisherVisibility();
    listing.updateMetadata(
        normalizeRequired(request.getPublicTitle(), "Public title is required."),
        normalizeOptional(request.getDescription()),
        normalizeTags(request.getTags()),
        normalizeOptional(request.getInstitution()),
        publisherVisibility,
        publisherDisplayName(listing, publisherVisibility));

    marketplaceListingRepository.flush();

    return toManagementDetailDto(listing, publisherId);
  }

  @Transactional
  public MarketplaceListingManagementDetailDto unlist(Long listingId, Long publisherId) {
    MarketplaceListing listing = getOwnedListingOrThrow(listingId, publisherId);

    if (listing.getStatus() == MarketplaceListingStatus.UNLISTED) {
      return toManagementDetailDto(listing, publisherId);
    }

    if (listing.getStatus() != MarketplaceListingStatus.PUBLISHED) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only published listings can be unlisted.");
    }

    listing.unlist(java.time.Instant.now());

    marketplaceListingRepository.flush();

    return toManagementDetailDto(listing, publisherId);
  }

  @Transactional
  public MarketplaceListingManagementDetailDto republish(Long listingId, Long publisherId) {
    MarketplaceListing listing = getOwnedListingOrThrow(listingId, publisherId);

    // Validation checks on listing before we do anything
    if (listing.getStatus() == MarketplaceListingStatus.REMOVED) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Removed listings cannot be republished.");
    }

    TC sourceTc = tcRepository.findWithSourceMetadataByIdAndOwnerId(listing.getSourceTcId(), publisherId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Source topic sheet no longer exists."));

    // Thanks to dhruv for this edge case check
    if (isTcStale(sourceTc)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stale topic sheets cannot be republished.");
    }

    List<CFCEntry> publishableEntries = sortedEntries(sourceTc);
    if (publishableEntries.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This topic sheet has no entries to republish.");
    }

    // Erase then repopulate
    listing.clearEntries();
    for (int index = 0; index < publishableEntries.size(); index++) {
      CFCEntry entry = publishableEntries.get(index);

      // Constructor handles inserting itself into listing
      new MarketplaceListingEntrySnapshot(
          listing,
          entry.getId(),
          entry.getGeneratedCFCPage().getFlashcardQuestion(),
          entry.getGeneratedCFCPage().getFlashcardNoteContent(),
          index,
          entry.getCreatedAt());
    }

    // Flush all pending changes to database
    marketplaceListingRepository.flush();

    return toManagementDetailDto(listing, publisherId);
  }

  private MarketplaceListing getOwnedListingOrThrow(Long listingId, Long publisherId) {
    return marketplaceListingRepository.findByIdAndPublisherId(listingId, publisherId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Marketplace listing not found."));
  }

  // Mappings into DTOs !!
  private MarketplaceListingManagementSummaryDto toManagementSummaryDto(MarketplaceListing listing) {
    return new MarketplaceListingManagementSummaryDto(
        listing.getId(),
        listing.getPublicTitle(),
        listing.getCourseCode(),
        listing.getSchoolSem(),
        listing.getTopic(),
        listing.getStatus(),
        listing.getEntryCount(),
        listing.getUpvoteCount(),
        listing.getImportCount(),
        listing.getPublishedAt(),
        listing.getUpdatedAt(),
        listing.getUnlistedAt());
  }

  private MarketplaceListingManagementDetailDto toManagementDetailDto(MarketplaceListing listing, Long publisherId) {
    Optional<TC> sourceTc = tcRepository.findWithSourceMetadataByIdAndOwnerId(listing.getSourceTcId(), publisherId);
    boolean sourceTcStillExists = sourceTc.isPresent();
    boolean sourceTcIsStale = sourceTc.map(tc -> isTcStale(tc)).orElse(true);

    List<MarketplaceListingEntrySnapshotDto> entries = listing.getEntries()
        .stream()
        .sorted(Comparator.comparing(MarketplaceListingEntrySnapshot::getDisplayOrder))
        .map(entry -> toEntryDto(entry))
        .toList();

    return new MarketplaceListingManagementDetailDto(
        listing.getId(),
        listing.getPublicTitle(),
        listing.getDescription(),
        listing.getCourseCode(),
        listing.getSchoolSem(),
        listing.getTopic(),
        splitTags(listing.getTags()),
        listing.getInstitution(),
        listing.getPublisherVisibility(),
        listing.getStatus(),
        listing.getEntryCount(),
        listing.getUpvoteCount(),
        listing.getImportCount(),
        listing.getPublishedAt(),
        listing.getUpdatedAt(),
        listing.getUnlistedAt(),
        listing.getSourceTcId(),
        sourceTcStillExists,
        sourceTcIsStale,
        entries);
  }

  private MarketplaceListingEntrySnapshotDto toEntryDto(MarketplaceListingEntrySnapshot entry) {
    return new MarketplaceListingEntrySnapshotDto(
        entry.getId(),
        entry.getFlashcardQuestion(),
        entry.getFlashcardNoteContent(),
        entry.getDisplayOrder());
  }

  private List<CFCEntry> sortedEntries(TC tc) {
    return tc.getEntries()
        .stream()
        // Compare according to creation time
        .sorted(Comparator.comparing(CFCEntry::getCreatedAt).reversed())
        .toList();
  }

  private Boolean isTcStale(TC tc) {
    String tcTopic = normalizeForComparison(tc.getTopic());

    return tc.getModule()
        .getTopics()
        .stream()
        .map(topicEntity -> topicEntity.getTopicName())
        .map(topicName -> normalizeForComparison(topicName))
        .noneMatch(topic -> topic.equals(tcTopic));
  }

  private String publisherDisplayName(MarketplaceListing listing, PublisherVisibility publisherVisibility) {
    if (publisherVisibility == PublisherVisibility.ANONYMOUS) {
      return "Anonymous";
    }

    return listing.getPublisher().getUsername().trim();
  }

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

  private String normalizeRequired(String value, String errorMessage) {
    String normalizedValue = normalizeOptional(value);

    if (normalizedValue == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errorMessage);
    }

    return normalizedValue;
  }

  private String normalizeOptional(String value) {
    if (value == null) {
      return null;
    }

    String normalizedValue = value.trim();
    return normalizedValue.isEmpty() ? null : normalizedValue;
  }

  private String normalizeTags(List<String> tags) {
    if (tags == null || tags.isEmpty()) {
      return null;
    }

    List<String> normalizedTags = new ArrayList<>();
    Set<String> seenTags = new HashSet<>();

    for (String tag : tags) {
      String normalizedTag = normalizeOptional(tag);

      if (normalizedTag == null) {
        continue;
      }

      String comparisonKey = normalizedTag.toLowerCase(Locale.ROOT);
      if (seenTags.add(comparisonKey)) {
        normalizedTags.add(normalizedTag);
      }
    }

    if (normalizedTags.size() > 10) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At most 10 tags are allowed.");
    }

    return normalizedTags.isEmpty() ? null : String.join(",", normalizedTags);
  }

  private List<String> splitTags(String tags) {
    if (tags == null || tags.isBlank()) {
      return List.of();
    }

    return Arrays.stream(tags.split(","))
        .map(tag -> tag.trim())
        .filter(tag -> !tag.isEmpty())
        .toList();
  }

  private String normalizeForComparison(String value) {
    return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
  }
}
