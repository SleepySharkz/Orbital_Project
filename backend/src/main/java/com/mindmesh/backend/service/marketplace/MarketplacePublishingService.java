package com.mindmesh.backend.service.marketplace;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.mindmesh.backend.dto.requests.marketplace.PublishMarketplaceListingRequestDto;
import com.mindmesh.backend.dto.responses.marketplace.MarketplaceListingPublishResponseDto;
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
public class MarketplacePublishingService {
  private final MarketplaceListingRepository marketplaceListingRepository;
  private final TCRepository tcRepository;

  public MarketplacePublishingService(
      MarketplaceListingRepository marketplaceListingRepository,
      TCRepository tcRepository) {
    this.marketplaceListingRepository = marketplaceListingRepository;
    this.tcRepository = tcRepository;
  }

  @Transactional
  public MarketplaceListingPublishResponseDto publishTC(PublishMarketplaceListingRequestDto request, Long userId) {
    Long tcId = request.getTcId();
    TC tc = tcRepository.findByIdAndOwnerId(tcId, userId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cheatsheet not found."));

    // We do not publish stale tc's
    if (isTcStale(tc)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stale topic sheets cannot be published.");
    }

    List<CFCEntry> publishableEntries = sortedEntries(tc);

    if (publishableEntries.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This topic sheet has no entries to publish.");
    }

    // Stop publishing if its already published, duh...
    if (marketplaceListingRepository.existsByPublisherIdAndSourceTcIdAndStatus(
        userId,
        tcId,
        MarketplaceListingStatus.PUBLISHED)) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT,
          "This topic sheet already has a published marketplace listing.");
    }

    // Create listing
    MarketplaceListing listing = new MarketplaceListing(
        tc.getOwner(),
        tc.getId(),
        tc.getModule().getId(),
        normalizeRequired(request.getPublicTitle(), "Public title is required."),
        normalizeOptional(request.getDescription()),
        tc.getModule().getCourseCode(),
        tc.getModule().getSchoolSem(),
        tc.getTopic(),
        normalizeTags(request.getTags()),
        normalizeOptional(request.getInstitution()),
        request.getPublisherVisibility(),
        publisherDisplayName(tc, request.getPublisherVisibility()));

    for (int index = 0; index < publishableEntries.size(); index++) {
      CFCEntry entry = publishableEntries.get(index);

      new MarketplaceListingEntrySnapshot(
          listing,
          entry.getId(),
          entry.getGeneratedCFCPage().getFlashcardQuestion(),
          entry.getGeneratedCFCPage().getFlashcardNoteContent(),
          index,
          entry.getCreatedAt());
    }

    MarketplaceListing savedListing = marketplaceListingRepository.save(listing);

    return new MarketplaceListingPublishResponseDto(
        savedListing.getId(),
        savedListing.getStatus(),
        savedListing.getPublicTitle(),
        savedListing.getEntryCount(),
        savedListing.getPublishedAt());
  }

  private List<CFCEntry> sortedEntries(TC tc) {
    return tc.getEntries()
        .stream()
        .sorted(Comparator.comparing(CFCEntry::getCreatedAt).reversed())
        .toList();
  }

  private Boolean isTcStale(TC tc) {
    String tcTopic = normalizeForComparison(tc.getTopic());

    return tc.getModule()
        .getTopics()
        .stream()
        .map(topicEntity -> topicEntity.getTopicName())
        // Normalise before comparing
        .map(topicName -> normalizeForComparison(topicName))
        .noneMatch(topic -> topic.equals(tcTopic));
  }

  private String publisherDisplayName(TC tc, PublisherVisibility publisherVisibility) {
    if (publisherVisibility == PublisherVisibility.ANONYMOUS) {
      return "Anonymous";
    }

    return tc.getOwner().getUsername().trim();
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

  private String normalizeForComparison(String value) {
    return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
  }
}
