package com.mindmesh.backend.service.marketplace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import com.mindmesh.backend.dto.requests.marketplace.UpdateMarketplaceListingMetadataRequestDto;
import com.mindmesh.backend.dto.responses.marketplace.MarketplaceListingManagementDetailDto;
import com.mindmesh.backend.dto.responses.marketplace.MarketplaceListingManagementPageResponseDto;
import com.mindmesh.backend.entity.CFC;
import com.mindmesh.backend.entity.CFCEntry;
import com.mindmesh.backend.entity.CourseModule;
import com.mindmesh.backend.entity.GeneratedCFCPage;
import com.mindmesh.backend.entity.MarketplaceListing;
import com.mindmesh.backend.entity.MarketplaceListingEntrySnapshot;
import com.mindmesh.backend.entity.ModuleTopic;
import com.mindmesh.backend.entity.TC;
import com.mindmesh.backend.entity.User;
import com.mindmesh.backend.enums.MarketplaceListingStatus;
import com.mindmesh.backend.enums.PublisherVisibility;
import com.mindmesh.backend.enums.SourceType;
import com.mindmesh.backend.repository.MarketplaceListingRepository;
import com.mindmesh.backend.repository.TCRepository;

@ExtendWith(MockitoExtension.class)
class MarketplaceListingManagementServiceTest {

  @Mock
  private MarketplaceListingRepository marketplaceListingRepository;

  @Mock
  private TCRepository tcRepository;

  private MarketplaceListingManagementService service;
  private User publisher;
  private CourseModule module;
  private TC sourceTc;
  private MarketplaceListing listing;

  @BeforeEach
  void setUp() {
    marketplaceListingRepository = org.mockito.Mockito.mock(MarketplaceListingRepository.class);
    tcRepository = org.mockito.Mockito.mock(TCRepository.class);
    service = new MarketplaceListingManagementService(marketplaceListingRepository, tcRepository);

    publisher = new User("Tauzih", "tauzih@example.com", "hashed");
    ReflectionTestUtils.setField(publisher, "id", 7L);

    module = new CourseModule(publisher, "CS2040", "Year 1 Sem 2", List.of());
    ReflectionTestUtils.setField(module, "id", 12L);
    module.addTopic(new ModuleTopic(null, "Trees"));

    sourceTc = new TC(module, publisher, "Trees");
    ReflectionTestUtils.setField(sourceTc, "id", 55L);
    addTcEntry(sourceTc, 201L, 1, "Original question 1", LocalDateTime.of(2026, 7, 10, 10, 0));
    addTcEntry(sourceTc, 202L, 2, "Original question 2", LocalDateTime.of(2026, 7, 11, 10, 0));

    listing = new MarketplaceListing(
        publisher,
        55L,
        12L,
        "Trees Guide",
        "Old description",
        "CS2040",
        "Year 1 Sem 2",
        "Trees",
        "trees",
        "NUS",
        PublisherVisibility.DISPLAY_NAME,
        "Tauzih");
    ReflectionTestUtils.setField(listing, "id", 500L);
    ReflectionTestUtils.setField(listing, "publishedAt", Instant.parse("2026-07-12T01:00:00Z"));
    ReflectionTestUtils.setField(listing, "updatedAt", Instant.parse("2026-07-12T01:00:00Z"));
    new MarketplaceListingEntrySnapshot(
        listing,
        101L,
        "Old question",
        "Old note",
        0,
        LocalDateTime.of(2026, 7, 9, 10, 0));
  }

  @Test
  void listMyListings_returnsPublisherListingsWithStatusAndStats() {
    when(marketplaceListingRepository.findByPublisherIdOrderByUpdatedAtDesc(
        org.mockito.Mockito.eq(7L),
        org.mockito.Mockito.any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(listing)));

    MarketplaceListingManagementPageResponseDto response = service.listMyListings(7L, 0, 12);

    assertEquals(1, response.getItems().size());
    assertEquals("Trees Guide", response.getItems().get(0).getPublicTitle());
    assertEquals(MarketplaceListingStatus.PUBLISHED, response.getItems().get(0).getStatus());
    assertEquals(1, response.getItems().get(0).getEntryCount());
    assertEquals(0, response.getItems().get(0).getUpvoteCount());
    assertEquals(0, response.getItems().get(0).getImportCount());
  }

  @Test
  void updateMetadata_changesMetadataWithoutChangingEntries() {
    UpdateMarketplaceListingMetadataRequestDto request = new UpdateMarketplaceListingMetadataRequestDto();
    request.setPublicTitle("Updated Trees Guide");
    request.setDescription("Updated description");
    request.setTags(List.of(" revision ", "trees", "Revision"));
    request.setInstitution("SOC");
    request.setPublisherVisibility(PublisherVisibility.ANONYMOUS);

    givenOwnedListing();
    givenSourceTcExists();

    MarketplaceListingManagementDetailDto response = service.updateMetadata(500L, 7L, request);

    assertEquals("Updated Trees Guide", response.getPublicTitle());
    assertEquals("Updated description", listing.getDescription());
    assertEquals("revision,trees", listing.getTags());
    assertEquals("SOC", listing.getInstitution());
    assertEquals(PublisherVisibility.ANONYMOUS, listing.getPublisherVisibility());
    assertEquals("Anonymous", listing.getPublisherDisplayName());
    assertEquals(1, listing.getEntries().size());
    assertEquals("Old question", listing.getEntries().get(0).getFlashcardQuestion());
    verify(marketplaceListingRepository).flush();
  }

  @Test
  void unlist_publishedListingBecomesUnlisted() {
    givenOwnedListing();
    givenSourceTcExists();

    MarketplaceListingManagementDetailDto response = service.unlist(500L, 7L);

    assertEquals(MarketplaceListingStatus.UNLISTED, response.getStatus());
    assertEquals(MarketplaceListingStatus.UNLISTED, listing.getStatus());
    assertTrue(listing.getUnlistedAt() != null);
    verify(marketplaceListingRepository).flush();
  }

  @Test
  void republish_replacesEntriesFromCurrentSourceTc() {
    givenOwnedListing();
    givenSourceTcExists();

    MarketplaceListingManagementDetailDto response = service.republish(500L, 7L);

    assertEquals(2, response.getEntryCount());
    assertEquals(2, listing.getEntries().size());
    assertEquals("Original question 2", listing.getEntries().get(0).getFlashcardQuestion());
    assertEquals("Original note 2", listing.getEntries().get(0).getFlashcardNoteContent());
    assertEquals("Original question 1", listing.getEntries().get(1).getFlashcardQuestion());
    assertEquals(0, listing.getUpvoteCount());
    assertEquals(0, listing.getImportCount());
    verify(marketplaceListingRepository).flush();
  }

  @Test
  void republish_removedListingIsRejected() {
    listing.remove(Instant.parse("2026-07-13T01:00:00Z"));
    givenOwnedListing();

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> service.republish(500L, 7L));

    assertEquals(400, exception.getStatusCode().value());
  }

  @Test
  void getMyListingDetail_nonPublisherGetsNotFound() {
    when(marketplaceListingRepository.findByIdAndPublisherId(500L, 99L))
        .thenReturn(Optional.empty());

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> service.getMyListingDetail(500L, 99L));

    assertEquals(404, exception.getStatusCode().value());
  }

  @Test
  void detailShowsSourceTcAvailabilityAndStaleness() {
    givenOwnedListing();
    givenSourceTcExists();

    MarketplaceListingManagementDetailDto response = service.getMyListingDetail(500L, 7L);

    assertTrue(response.getSourceTcStillExists());
    assertFalse(response.getSourceTcIsStale());
    assertEquals(55L, response.getSourceTcId());
  }

  private void givenOwnedListing() {
    when(marketplaceListingRepository.findByIdAndPublisherId(500L, 7L))
        .thenReturn(Optional.of(listing));
  }

  private void givenSourceTcExists() {
    when(tcRepository.findWithSourceMetadataByIdAndOwnerId(55L, 7L))
        .thenReturn(Optional.of(sourceTc));
  }

  private void addTcEntry(
      TC tc,
      Long entryId,
      long requestItemId,
      String flashcardQuestion,
      LocalDateTime createdAt) {
    CFC cfc = new CFC(
        module,
        SourceType.TUTORIAL,
        "Tutorial " + requestItemId,
        "Title " + requestItemId,
        "Summary " + requestItemId);

    CFCEntry entry = new CFCEntry(
        cfc,
        requestItemId,
        "Trees",
        "Private question " + requestItemId,
        "Private rough note " + requestItemId,
        new GeneratedCFCPage(flashcardQuestion, "Original note " + requestItemId));

    ReflectionTestUtils.setField(entry, "id", entryId);
    ReflectionTestUtils.setField(entry, "createdAt", createdAt);
    tc.addEntry(entry);
  }
}
