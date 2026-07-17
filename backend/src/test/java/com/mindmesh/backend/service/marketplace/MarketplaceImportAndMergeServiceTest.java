package com.mindmesh.backend.service.marketplace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.mindmesh.backend.dto.responses.marketplace.MarketplaceImportResponseDto;
import com.mindmesh.backend.entity.MarketplaceImport;
import com.mindmesh.backend.entity.MarketplaceListing;
import com.mindmesh.backend.entity.MarketplaceListingEntrySnapshot;
import com.mindmesh.backend.entity.User;
import com.mindmesh.backend.enums.MarketplaceListingStatus;
import com.mindmesh.backend.enums.PublisherVisibility;
import com.mindmesh.backend.repository.MarketplaceImportRepository;
import com.mindmesh.backend.repository.MarketplaceListingRepository;
import com.mindmesh.backend.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class MarketplaceImportAndMergeServiceTest {

  @Mock
  private MarketplaceImportRepository marketplaceImportRepository;

  @Mock
  private MarketplaceListingRepository marketplaceListingRepository;

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private MarketplaceImportAndMergeService marketplaceImportAndMergeService;

  private User importer;
  private MarketplaceListing listing;

  @BeforeEach
  void setUp() {
    User publisher = new User("Publisher", "publisher@example.com", "hashed");
    ReflectionTestUtils.setField(publisher, "id", 1L);

    importer = new User("Importer", "importer@example.com", "hashed");
    ReflectionTestUtils.setField(importer, "id", 2L);

    listing = new MarketplaceListing(
        publisher,
        10L,
        20L,
        "Trees Guide",
        "Public notes",
        "CS2040",
        "Year 1 Sem 2",
        "Trees",
        "trees",
        "NUS",
        PublisherVisibility.DISPLAY_NAME,
        "Publisher");
    ReflectionTestUtils.setField(listing, "id", 50L);

    MarketplaceListingEntrySnapshot firstEntry = new MarketplaceListingEntrySnapshot(
        listing,
        101L,
        "Question 1",
        "Note 1",
        0,
        LocalDateTime.of(2026, 7, 1, 10, 0));
    MarketplaceListingEntrySnapshot secondEntry = new MarketplaceListingEntrySnapshot(
        listing,
        102L,
        "Question 2",
        "Note 2",
        1,
        LocalDateTime.of(2026, 7, 2, 10, 0));
    ReflectionTestUtils.setField(firstEntry, "id", 201L);
    ReflectionTestUtils.setField(secondEntry, "id", 202L);
  }

  @Test
  void importListing_copiesSnapshotAndIncrementsCount() {
    when(marketplaceListingRepository.findByIdAndStatus(50L, MarketplaceListingStatus.PUBLISHED))
        .thenReturn(Optional.of(listing));
    when(marketplaceImportRepository.existsByImporterIdAndSourceListingId(2L, 50L))
        .thenReturn(false);
    when(userRepository.findById(2L)).thenReturn(Optional.of(importer));
    when(marketplaceImportRepository.saveAndFlush(any(MarketplaceImport.class)))
        .thenAnswer(invocation -> {
          MarketplaceImport marketplaceImport = invocation.getArgument(0);
          ReflectionTestUtils.setField(marketplaceImport, "id", 300L);
          ReflectionTestUtils.setField(
              marketplaceImport,
              "importedAt",
              Instant.parse("2026-07-17T10:00:00Z"));
          return marketplaceImport;
        });

    MarketplaceImportResponseDto response = marketplaceImportAndMergeService.importListing(50L, 2L);

    ArgumentCaptor<MarketplaceImport> importCaptor = ArgumentCaptor.forClass(MarketplaceImport.class);
    verify(marketplaceImportRepository).saveAndFlush(importCaptor.capture());
    MarketplaceImport savedImport = importCaptor.getValue();

    assertEquals(300L, response.getImportId());
    assertEquals(2, response.getEntryCount());
    assertEquals("Trees Guide", savedImport.getSourceListingTitle());
    assertEquals("Question 1", savedImport.getEntries().get(0).getFlashcardQuestion());
    assertEquals("Note 2", savedImport.getEntries().get(1).getFlashcardNoteContent());
    assertEquals(1, listing.getImportCount());
    verify(marketplaceListingRepository).save(listing);
  }
}
