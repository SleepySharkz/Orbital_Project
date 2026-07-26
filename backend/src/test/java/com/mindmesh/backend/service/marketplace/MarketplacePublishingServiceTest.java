package com.mindmesh.backend.service.marketplace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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
import org.springframework.web.server.ResponseStatusException;

import com.mindmesh.backend.dto.requests.marketplace.PublishMarketplaceListingRequestDto;
import com.mindmesh.backend.dto.responses.marketplace.MarketplaceListingPublishResponseDto;
import com.mindmesh.backend.entity.CFC;
import com.mindmesh.backend.entity.CFCEntry;
import com.mindmesh.backend.entity.CourseModule;
import com.mindmesh.backend.entity.GeneratedCFCPage;
import com.mindmesh.backend.entity.MarketplaceListing;
import com.mindmesh.backend.entity.ModuleTopic;
import com.mindmesh.backend.entity.TC;
import com.mindmesh.backend.entity.User;
import com.mindmesh.backend.enums.MarketplaceListingStatus;
import com.mindmesh.backend.enums.PublisherVisibility;
import com.mindmesh.backend.enums.SourceType;
import com.mindmesh.backend.repository.MarketplaceListingRepository;
import com.mindmesh.backend.repository.TCRepository;

@ExtendWith(MockitoExtension.class)
class MarketplacePublishingServiceTest {

  @Mock
  private MarketplaceListingRepository marketplaceListingRepository;

  @Mock
  private TCRepository tcRepository;

  @InjectMocks
  private MarketplacePublishingService marketplacePublishingService;

  private User owner;
  private CourseModule module;
  private TC tc;

  @BeforeEach
  void setUp() {
    owner = new User("Tauzih", "tauzih@example.com", "hashed");
    ReflectionTestUtils.setField(owner, "id", 7L);

    module = new CourseModule(owner, "CS2040", "Year 1 Sem 2", List.of());
    ReflectionTestUtils.setField(module, "id", 12L);
    module.addTopic(new ModuleTopic(null, "Trees"));

    tc = new TC(module, owner, "Trees");
    ReflectionTestUtils.setField(tc, "id", 55L);
    addEntry(tc, 101L, 1, LocalDateTime.of(2026, 7, 12, 10, 0));
    addEntry(tc, 102L, 2, LocalDateTime.of(2026, 7, 13, 10, 0));
  }

  @Test
  void publishTc_withOwnedActiveTc_createsPublicSnapshot() {
    PublishMarketplaceListingRequestDto request = buildRequest(PublisherVisibility.DISPLAY_NAME);

    givenOwnedTcExists(tc);
    givenNoPublishedListingExists();
    saveListingWithGeneratedId();

    MarketplaceListingPublishResponseDto response =
        marketplacePublishingService.publishTC(request, 7L);

    MarketplaceListing savedListing = savedListing();
    assertEquals(owner, savedListing.getPublisher());
    assertEquals(55L, savedListing.getSourceTcId());
    assertEquals(12L, savedListing.getSourceModuleId());
    assertEquals("BST Revision Pack", savedListing.getPublicTitle());
    assertEquals("Public description", savedListing.getDescription());
    assertEquals("CS2040", savedListing.getCourseCode());
    assertEquals("Year 1 Sem 2", savedListing.getSchoolSem());
    assertEquals("Trees", savedListing.getTopic());
    assertEquals("bst,trees", savedListing.getTags());
    assertEquals("NUS", savedListing.getInstitution());
    assertEquals(PublisherVisibility.DISPLAY_NAME, savedListing.getPublisherVisibility());
    assertEquals("Tauzih", savedListing.getPublisherDisplayName());
    assertEquals(MarketplaceListingStatus.PUBLISHED, savedListing.getStatus());
    assertEquals(2, savedListing.getEntryCount());
    assertEquals(2, savedListing.getEntries().size());
    assertEquals(102L, savedListing.getEntries().get(0).getSourceEntryId());
    assertEquals("Flashcard question 2", savedListing.getEntries().get(0).getFlashcardQuestion());
    assertEquals("Flashcard note content 2", savedListing.getEntries().get(0).getFlashcardNoteContent());
    assertEquals(0, savedListing.getEntries().get(0).getDisplayOrder());
    assertEquals(101L, savedListing.getEntries().get(1).getSourceEntryId());

    assertEquals(500L, response.getListingId());
    assertEquals(MarketplaceListingStatus.PUBLISHED, response.getStatus());
    assertEquals("BST Revision Pack", response.getPublicTitle());
    assertEquals(2, response.getEntryCount());
    assertNotNull(response.getPublishedAt());
  }

  @Test
  void publishTc_withStaleTc_throwsBadRequest() {
    PublishMarketplaceListingRequestDto request = buildRequest(PublisherVisibility.DISPLAY_NAME);
    module.removeTopic(module.getTopics().get(0));
    givenOwnedTcExists(tc);

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> marketplacePublishingService.publishTC(request, 7L));

    assertEquals(400, exception.getStatusCode().value());
    assertTrue(exception.getReason().contains("Stale topic sheets"));
    verify(marketplaceListingRepository, never()).save(any(MarketplaceListing.class));
  }

  @Test
  void publishTc_withExistingPublishedListing_throwsConflict() {
    PublishMarketplaceListingRequestDto request = buildRequest(PublisherVisibility.DISPLAY_NAME);
    givenOwnedTcExists(tc);
    givenPublishedListingAlreadyExists();

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> marketplacePublishingService.publishTC(request, 7L));

    assertEquals(409, exception.getStatusCode().value());
    verify(marketplaceListingRepository, never()).save(any(MarketplaceListing.class));
  }

  private PublishMarketplaceListingRequestDto buildRequest(PublisherVisibility visibility) {
    PublishMarketplaceListingRequestDto request = new PublishMarketplaceListingRequestDto();
    request.setTcId(55L);
    request.setPublicTitle("  BST Revision Pack  ");
    request.setDescription("  Public description  ");
    request.setTags(List.of(" bst ", "trees", "BST"));
    request.setInstitution("  NUS  ");
    request.setPublisherVisibility(visibility);
    return request;
  }

  private void givenOwnedTcExists(TC ownedTc) {
    when(tcRepository.findByIdAndOwnerId(ownedTc.getId(), 7L))
        .thenReturn(Optional.of(ownedTc));
  }

  private void givenNoPublishedListingExists() {
    when(marketplaceListingRepository.existsByPublisherIdAndSourceTcIdAndStatus(
        7L,
        55L,
        MarketplaceListingStatus.PUBLISHED))
        .thenReturn(false);
  }

  private void givenPublishedListingAlreadyExists() {
    when(marketplaceListingRepository.existsByPublisherIdAndSourceTcIdAndStatus(
        7L,
        55L,
        MarketplaceListingStatus.PUBLISHED))
        .thenReturn(true);
  }

  private void saveListingWithGeneratedId() {
    when(marketplaceListingRepository.save(any(MarketplaceListing.class)))
        .thenAnswer(invocation -> {
          MarketplaceListing listing = invocation.getArgument(0);
          ReflectionTestUtils.setField(listing, "id", 500L);
          ReflectionTestUtils.setField(listing, "publishedAt", Instant.parse("2026-07-13T02:00:00Z"));
          return listing;
        });
  }

  private MarketplaceListing savedListing() {
    ArgumentCaptor<MarketplaceListing> listingCaptor =
        ArgumentCaptor.forClass(MarketplaceListing.class);
    verify(marketplaceListingRepository).save(listingCaptor.capture());
    return listingCaptor.getValue();
  }

  private void addEntry(TC tc, Long entryId, long requestItemId, LocalDateTime createdAt) {
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
        new GeneratedCFCPage(
            "Flashcard question " + requestItemId,
            "Flashcard note content " + requestItemId));

    ReflectionTestUtils.setField(entry, "id", entryId);
    ReflectionTestUtils.setField(entry, "createdAt", createdAt);
    tc.addEntry(entry);
  }
}
