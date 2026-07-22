package com.mindmesh.backend.service.marketplace;

import java.util.Comparator;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.mindmesh.backend.dto.responses.marketplace.MarketplaceImportDetailDto;
import com.mindmesh.backend.dto.responses.marketplace.MarketplaceImportedEntryDto;
import com.mindmesh.backend.dto.responses.marketplace.MarketplaceImportResponseDto;
import com.mindmesh.backend.dto.responses.marketplace.MarketplaceImportSummaryDto;
import com.mindmesh.backend.entity.MarketplaceImport;
import com.mindmesh.backend.entity.MarketplaceImportedEntry;
import com.mindmesh.backend.entity.MarketplaceListing;
import com.mindmesh.backend.entity.MarketplaceListingEntrySnapshot;
import com.mindmesh.backend.entity.User;
import com.mindmesh.backend.enums.MarketplaceListingStatus;
import com.mindmesh.backend.repository.MarketplaceImportRepository;
import com.mindmesh.backend.repository.MarketplaceListingRepository;
import com.mindmesh.backend.repository.UserRepository;

@Service
public class MarketplaceImportAndMergeService {

  private final MarketplaceImportRepository marketplaceImportRepository;
  private final MarketplaceListingRepository marketplaceListingRepository;
  private final UserRepository userRepository;

  public MarketplaceImportAndMergeService(
      MarketplaceImportRepository marketplaceImportRepository,
      MarketplaceListingRepository marketplaceListingRepository,
      UserRepository userRepository) {
    this.marketplaceImportRepository = marketplaceImportRepository;
    this.marketplaceListingRepository = marketplaceListingRepository;
    this.userRepository = userRepository;
  }

  @Transactional
  public MarketplaceImportResponseDto importListing(Long listingId, Long importerId) {
    MarketplaceListing listing = marketplaceListingRepository
        .findByIdAndStatus(listingId, MarketplaceListingStatus.PUBLISHED)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Marketplace listing not found."));

    if (marketplaceImportRepository.existsByImporterIdAndSourceListingId(importerId, listingId)) {
      throw duplicateImport();
    }

    User importer = userRepository.findById(importerId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));

    MarketplaceImport marketplaceImport = new MarketplaceImport(importer, listing);

    for (MarketplaceListingEntrySnapshot entry : sortedEntries(listing)) {
      new MarketplaceImportedEntry(
          marketplaceImport,
          entry.getId(),
          entry.getFlashcardQuestion(),
          entry.getFlashcardNoteContent(),
          entry.getDisplayOrder());
    }

    MarketplaceImport savedImport;
    try {
      // 2 in one cool method
      savedImport = marketplaceImportRepository.saveAndFlush(marketplaceImport);
    } catch (DataIntegrityViolationException exception) {
      throw duplicateImport();
    }

    // Since it is transactional, dont have to worry about concurrency issues
    listing.incrementImportCount();
    marketplaceListingRepository.save(listing);

    return toImportResponseDto(savedImport);
  }

  @Transactional(readOnly = true)
  public List<MarketplaceImportSummaryDto> listImports(Long importerId) {
    return marketplaceImportRepository
        .findByImporterIdOrderByImportedAtDesc(importerId)
        .stream()
        .map(importEntity -> toSummaryDto(importEntity))
        .toList();
  }

  @Transactional(readOnly = true)
  public MarketplaceImportDetailDto getImportDetail(Long importId, Long importerId) {
    MarketplaceImport marketplaceImport = marketplaceImportRepository
        .findByIdAndImporterId(importId, importerId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Marketplace import not found."));

    return toDetailDto(marketplaceImport);
  }

  private List<MarketplaceListingEntrySnapshot> sortedEntries(MarketplaceListing listing) {
    return listing.getEntries()
        .stream()
        .sorted(Comparator.comparing(MarketplaceListingEntrySnapshot::getDisplayOrder))
        .toList();
  }

  private MarketplaceImportResponseDto toImportResponseDto(MarketplaceImport marketplaceImport) {
    return new MarketplaceImportResponseDto(
        marketplaceImport.getId(),
        marketplaceImport.getSourceListing().getId(),
        marketplaceImport.getSourceListingTitle(),
        marketplaceImport.getEntries().size(),
        marketplaceImport.getImportedAt());
  }

  private MarketplaceImportSummaryDto toSummaryDto(MarketplaceImport marketplaceImport) {
    return new MarketplaceImportSummaryDto(
        marketplaceImport.getId(),
        marketplaceImport.getSourceListingTitle(),
        marketplaceImport.getSourcePublisherDisplayName(),
        marketplaceImport.getCourseCode(),
        marketplaceImport.getSchoolSem(),
        marketplaceImport.getTopic(),
        marketplaceImport.getEntries().size(),
        marketplaceImport.getImportedAt());
  }

  private MarketplaceImportDetailDto toDetailDto(MarketplaceImport marketplaceImport) {
    List<MarketplaceImportedEntryDto> entries = marketplaceImport.getEntries()
        .stream()
        .sorted(Comparator.comparing(MarketplaceImportedEntry::getDisplayOrder))
        .map(this::toEntryDto)
        .toList();

    return new MarketplaceImportDetailDto(
        marketplaceImport.getId(),
        marketplaceImport.getSourceListing().getId(),
        marketplaceImport.getSourceListingTitle(),
        marketplaceImport.getSourcePublisherDisplayName(),
        marketplaceImport.getCourseCode(),
        marketplaceImport.getSchoolSem(),
        marketplaceImport.getTopic(),
        entries.size(),
        marketplaceImport.getImportedAt(),
        entries);
  }

  private MarketplaceImportedEntryDto toEntryDto(MarketplaceImportedEntry entry) {
    return new MarketplaceImportedEntryDto(
        entry.getId(),
        entry.getFlashcardQuestion(),
        entry.getFlashcardNoteContent(),
        entry.getDisplayOrder());
  }

  private ResponseStatusException duplicateImport() {
    return new ResponseStatusException(
        HttpStatus.CONFLICT,
        "You have already imported this marketplace listing.");
  }
}
