package com.mindmesh.backend.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

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
import com.mindmesh.backend.repository.CFCEntryRepository;
import com.mindmesh.backend.repository.CFCRepository;
import com.mindmesh.backend.repository.CourseModuleRepository;
import com.mindmesh.backend.repository.MarketplaceImportRepository;
import com.mindmesh.backend.repository.MarketplaceListingReportRepository;
import com.mindmesh.backend.repository.MarketplaceListingRepository;
import com.mindmesh.backend.repository.TCRepository;
import com.mindmesh.backend.repository.UserRepository;
import com.mindmesh.backend.security.CustomUserDetails;

@SpringBootTest
@ActiveProfiles({ "test", "local-ai-fake" })
class MarketplaceControllerIntegrationTest {

  @Autowired
  private WebApplicationContext webApplicationContext;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private CourseModuleRepository courseModuleRepository;

  @Autowired
  private CFCRepository cfcRepository;

  @Autowired
  private CFCEntryRepository cfcEntryRepository;

  @Autowired
  private TCRepository tcRepository;

  @Autowired
  private MarketplaceListingRepository marketplaceListingRepository;

  @Autowired
  private MarketplaceImportRepository marketplaceImportRepository;

  @Autowired
  private MarketplaceListingReportRepository marketplaceListingReportRepository;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders
        .webAppContextSetup(webApplicationContext)
        .apply(org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity())
        .build();

    cleanDatabaseState();
  }

  @AfterEach
  void tearDown() {
    cleanDatabaseState();
  }

  private void cleanDatabaseState() {
    marketplaceImportRepository.deleteAll();
    marketplaceListingReportRepository.deleteAll();
    marketplaceListingRepository.deleteAll();
    cfcRepository.deleteAll();
    tcRepository.deleteAll();
    courseModuleRepository.deleteAll();
    userRepository.deleteAll();
  }

  @Test
  void publishListing_withOwnedActiveTc_createsMarketplaceSnapshot() throws Exception {
    User user = userRepository.save(new User("Tauzih", "tauzih@example.com", "hashed"));
    CourseModule module = saveModule(user, "Trees");
    TC tc = saveTcWithEntries(module, user, "Trees", 2);

    publishListing(
        user,
        """
            {
              "tcId": %d,
              "publicTitle": "BST Revision Pack",
              "description": "Useful public summary",
              "tags": ["bst", "trees", "BST"],
              "institution": "NUS",
              "publisherVisibility": "DISPLAY_NAME"
            }
            """.formatted(tc.getId()))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.listingId").isNumber())
        .andExpect(jsonPath("$.status").value("PUBLISHED"))
        .andExpect(jsonPath("$.publicTitle").value("BST Revision Pack"))
        .andExpect(jsonPath("$.entryCount").value(2))
        .andExpect(jsonPath("$.publishedAt").isNotEmpty());

    assertEquals(1L, marketplaceListingRepository.count());
    MarketplaceListing listing = savedListingFor(user);

    assertEquals(user.getId(), listing.getPublisher().getId());
    assertEquals(tc.getId(), listing.getSourceTcId());
    assertEquals(module.getId(), listing.getSourceModuleId());
    assertEquals("CS2040", listing.getCourseCode());
    assertEquals("Trees", listing.getTopic());
    assertEquals("bst,trees", listing.getTags());
    assertEquals("NUS", listing.getInstitution());
    assertEquals(PublisherVisibility.DISPLAY_NAME, listing.getPublisherVisibility());
    assertEquals("Tauzih", listing.getPublisherDisplayName());
    assertEquals(MarketplaceListingStatus.PUBLISHED, listing.getStatus());
    assertEquals(2, listing.getEntries().size());
    assertEquals("Flashcard question 2", listing.getEntries().get(0).getFlashcardQuestion());
    assertEquals("Flashcard note content 2", listing.getEntries().get(0).getFlashcardNoteContent());
    assertEquals(0, listing.getEntries().get(0).getDisplayOrder());
  }

  @Test
  void publishListing_withOtherUsersTc_returnsNotFound() throws Exception {
    User owner = userRepository.save(new User("Tauzih", "tauzih@example.com", "hashed"));
    User otherUser = userRepository.save(new User("Dhruv", "dhruv@example.com", "hashed"));
    CourseModule module = saveModule(owner, "Trees");
    TC tc = saveTcWithEntries(module, owner, "Trees", 1);

    publishListing(
        otherUser,
        publishRequestJson(tc, "Should Not Publish"))
        .andExpect(status().isNotFound());

    assertEquals(0L, marketplaceListingRepository.count());
  }

  @Test
  void browseListings_returnsOnlyPublishedMatchingListingsWithPageMetadata() throws Exception {
    User user = userRepository.save(new User("Tauzih", "tauzih@example.com", "hashed"));
    CourseModule module = saveModule(user, "Trees", "Heaps");
    TC visibleTc = saveTcWithEntries(module, user, "Trees", 1);
    TC hiddenTc = saveTcWithEntries(module, user, "Heaps", 1);

    publishListing(
        user,
        """
            {
              "tcId": %d,
              "publicTitle": "BST Revision Pack",
              "description": "Binary search tree drills",
              "tags": ["trees"],
              "publisherVisibility": "DISPLAY_NAME"
            }
            """.formatted(visibleTc.getId()))
        .andExpect(status().isCreated());

    publishListing(user, publishRequestJson(hiddenTc, "Hidden BST Pack"))
        .andExpect(status().isCreated());

    MarketplaceListing hiddenListing = marketplaceListingRepository
        .findByPublisherIdOrderByPublishedAtDesc(user.getId(), Pageable.unpaged())
        .getContent()
        .stream()
        .filter(listing -> listing.getPublicTitle().equals("Hidden BST Pack"))
        .findFirst()
        .orElseThrow();
    hiddenListing.unlist(Instant.now());
    marketplaceListingRepository.save(hiddenListing);

    mockMvc.perform(get("/api/v1/marketplace/listings")
        .with(authentication(authFor(user)))
        .param("q", "binary")
        .param("page", "0")
        .param("size", "12")
        .param("sort", "newest"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items", hasSize(1)))
        .andExpect(jsonPath("$.items[0].publicTitle").value("BST Revision Pack"))
        .andExpect(jsonPath("$.items[0].descriptionPreview").value("Binary search tree drills"))
        .andExpect(jsonPath("$.items[0].courseCode").value("CS2040"))
        .andExpect(jsonPath("$.items[0].topic").value("Trees"))
        .andExpect(jsonPath("$.page").value(0))
        .andExpect(jsonPath("$.size").value(12))
        .andExpect(jsonPath("$.totalItems").value(1))
        .andExpect(jsonPath("$.totalPages").value(1))
        .andExpect(jsonPath("$.hasNext").value(false));
  }

  @Test
  void getListingDetail_returnsPublishedEntriesWithoutPrivateSourceFields() throws Exception {
    User user = userRepository.save(new User("Tauzih", "tauzih@example.com", "hashed"));
    CourseModule module = saveModule(user, "Trees");
    TC tc = saveTcWithEntries(module, user, "Trees", 2);

    publishListing(
        user,
        """
            {
              "tcId": %d,
              "publicTitle": "BST Revision Pack",
              "description": "Useful public summary",
              "tags": ["trees"],
              "institution": "NUS",
              "publisherVisibility": "DISPLAY_NAME"
            }
            """.formatted(tc.getId()))
        .andExpect(status().isCreated());

    MarketplaceListing listing = savedListingFor(user);

    mockMvc.perform(get("/api/v1/marketplace/listings/{listingId}", listing.getId())
        .with(authentication(authFor(user))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.publicTitle").value("BST Revision Pack"))
        .andExpect(jsonPath("$.description").value("Useful public summary"))
        .andExpect(jsonPath("$.entries", hasSize(2)))
        .andExpect(jsonPath("$.entries[0].flashcardQuestion").value("Flashcard question 2"))
        .andExpect(jsonPath("$.entries[0].flashcardNoteContent").value("Flashcard note content 2"))
        .andExpect(jsonPath("$.entries[0].displayOrder").value(0))
        .andExpect(jsonPath("$.sourceTcId").doesNotExist())
        .andExpect(jsonPath("$.sourceModuleId").doesNotExist())
        .andExpect(jsonPath("$.status").doesNotExist())
        .andExpect(jsonPath("$.entries[0].sourceEntryId").doesNotExist())
        .andExpect(jsonPath("$.entries[0].sourceEntryCreatedAt").doesNotExist());
  }

  @Test
  void getListingDetail_returnsUpvoteStateForCurrentUser() throws Exception {
    User publisher = userRepository.save(new User("Tauzih", "tauzih@example.com", "hashed"));
    User viewer = userRepository.save(new User("Dhruv", "dhruv@example.com", "hashed"));
    CourseModule module = saveModule(publisher, "Trees");
    TC tc = saveTcWithEntries(module, publisher, "Trees", 1);

    publishListing(publisher, publishRequestJson(tc, "BST Revision Pack"))
        .andExpect(status().isCreated());

    MarketplaceListing listing = savedListingFor(publisher);

    mockMvc.perform(post("/api/v1/marketplace/listings/{listingId}/upvote", listing.getId())
        .with(authentication(authFor(viewer))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.hasCurrentUserUpvoted").value(true));

    mockMvc.perform(get("/api/v1/marketplace/listings/{listingId}", listing.getId())
        .with(authentication(authFor(viewer))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.upvoteCount").value(1))
        .andExpect(jsonPath("$.hasCurrentUserUpvoted").value(true));

    mockMvc.perform(get("/api/v1/marketplace/listings/{listingId}", listing.getId())
        .with(authentication(authFor(publisher))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.upvoteCount").value(1))
        .andExpect(jsonPath("$.hasCurrentUserUpvoted").value(false));
  }

  @Test
  void myListings_returnsOnlyCurrentPublishersListingsAcrossStatuses() throws Exception {
    User publisher = userRepository.save(new User("Tauzih", "tauzih@example.com", "hashed"));
    User otherUser = userRepository.save(new User("Dhruv", "dhruv@example.com", "hashed"));
    CourseModule publisherModule = saveModule(publisher, "Trees", "Graphs");
    CourseModule otherModule = saveModule(otherUser, "Trees");
    TC publishedTc = saveTcWithEntries(publisherModule, publisher, "Trees", 1);
    TC unlistedTc = saveTcWithEntries(publisherModule, publisher, "Graphs", 1);
    TC otherTc = saveTcWithEntries(otherModule, otherUser, "Trees", 1);

    publishListing(publisher, publishRequestJson(publishedTc, "Published Listing"))
        .andExpect(status().isCreated());
    publishListing(publisher, publishRequestJson(unlistedTc, "Unlisted Listing"))
        .andExpect(status().isCreated());
    publishListing(otherUser, publishRequestJson(otherTc, "Other Listing"))
        .andExpect(status().isCreated());

    MarketplaceListing unlistedListing = marketplaceListingRepository
        .findByPublisherIdOrderByPublishedAtDesc(publisher.getId(), Pageable.unpaged())
        .getContent()
        .stream()
        .filter(listing -> listing.getPublicTitle().equals("Unlisted Listing"))
        .findFirst()
        .orElseThrow();
    unlistedListing.unlist(Instant.now());
    marketplaceListingRepository.save(unlistedListing);

    mockMvc.perform(get("/api/v1/marketplace/my-listings")
        .with(authentication(authFor(publisher))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items", hasSize(2)))
        .andExpect(jsonPath("$.items[?(@.publicTitle == 'Published Listing')]").exists())
        .andExpect(jsonPath("$.items[?(@.publicTitle == 'Unlisted Listing')]").exists())
        .andExpect(jsonPath("$.items[?(@.publicTitle == 'Other Listing')]").doesNotExist());
  }

  @Test
  void patchMyListing_updatesMetadataWithoutChangingEntries() throws Exception {
    User publisher = userRepository.save(new User("Tauzih", "tauzih@example.com", "hashed"));
    CourseModule module = saveModule(publisher, "Trees");
    TC tc = saveTcWithEntries(module, publisher, "Trees", 1);
    publishListing(publisher, publishRequestJson(tc, "Trees Guide"))
        .andExpect(status().isCreated());
    MarketplaceListing listing = savedListingFor(publisher);

    mockMvc.perform(patch("/api/v1/marketplace/my-listings/{listingId}", listing.getId())
        .with(authentication(authFor(publisher)))
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "publicTitle": "Updated Trees Guide",
              "description": "Updated public description",
              "tags": ["revision", "trees", "Revision"],
              "institution": "SOC",
              "publisherVisibility": "ANONYMOUS"
            }
            """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.publicTitle").value("Updated Trees Guide"))
        .andExpect(jsonPath("$.description").value("Updated public description"))
        .andExpect(jsonPath("$.tags", hasSize(2)))
        .andExpect(jsonPath("$.institution").value("SOC"))
        .andExpect(jsonPath("$.publisherVisibility").value("ANONYMOUS"))
        .andExpect(jsonPath("$.entries[0].flashcardQuestion").value("Flashcard question 1"));

    MarketplaceListing updatedListing = marketplaceListingRepository
        .findByIdAndPublisherId(listing.getId(), publisher.getId())
        .orElseThrow();
    assertEquals("revision,trees", updatedListing.getTags());
    assertEquals("Anonymous", updatedListing.getPublisherDisplayName());
    assertEquals(1, updatedListing.getEntries().size());
  }

  @Test
  void unlistMyListing_hidesListingFromPublicBrowseAndDetail() throws Exception {
    User publisher = userRepository.save(new User("Tauzih", "tauzih@example.com", "hashed"));
    CourseModule module = saveModule(publisher, "Trees");
    TC tc = saveTcWithEntries(module, publisher, "Trees", 1);
    publishListing(publisher, publishRequestJson(tc, "Trees Guide"))
        .andExpect(status().isCreated());
    MarketplaceListing listing = savedListingFor(publisher);

    mockMvc.perform(post("/api/v1/marketplace/my-listings/{listingId}/unlist", listing.getId())
        .with(authentication(authFor(publisher))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("UNLISTED"))
        .andExpect(jsonPath("$.unlistedAt").isNotEmpty());

    mockMvc.perform(get("/api/v1/marketplace/listings")
        .with(authentication(authFor(publisher))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items", hasSize(0)));

    mockMvc.perform(get("/api/v1/marketplace/listings/{listingId}", listing.getId())
        .with(authentication(authFor(publisher))))
        .andExpect(status().isNotFound());
  }

  @Test
  void republishMyListing_replacesSnapshotsFromCurrentSourceTc() throws Exception {
    User publisher = userRepository.save(new User("Tauzih", "tauzih@example.com", "hashed"));
    CourseModule module = saveModule(publisher, "Trees");
    TC tc = saveTcWithEntries(module, publisher, "Trees", 1);
    publishListing(publisher, publishRequestJson(tc, "Trees Guide"))
        .andExpect(status().isCreated());
    MarketplaceListing listing = savedListingFor(publisher);

    saveAdditionalTcEntry(tc, module, "Trees", 2);

    mockMvc.perform(post("/api/v1/marketplace/my-listings/{listingId}/republish", listing.getId())
        .with(authentication(authFor(publisher))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(listing.getId()))
        .andExpect(jsonPath("$.publicTitle").value("Trees Guide"))
        .andExpect(jsonPath("$.entryCount").value(2))
        .andExpect(jsonPath("$.upvoteCount").value(0))
        .andExpect(jsonPath("$.importCount").value(0))
        .andExpect(jsonPath("$.entries", hasSize(2)))
        .andExpect(jsonPath("$.entries[0].flashcardQuestion").value("Flashcard question 2"));

    MarketplaceListing republishedListing = marketplaceListingRepository
        .findByIdAndPublisherId(listing.getId(), publisher.getId())
        .orElseThrow();
    assertEquals(2, republishedListing.getEntries().size());
  }

  @Test
  void importListing_createsOwnedCopyAndReturnsItFromImportReads() throws Exception {
    User publisher = userRepository.save(new User("Tauzih", "tauzih@example.com", "hashed"));
    User importer = userRepository.save(new User("Dhruv", "dhruv@example.com", "hashed"));
    CourseModule module = saveModule(publisher, "Trees");
    TC tc = saveTcWithEntries(module, publisher, "Trees", 2);
    publishListing(publisher, publishRequestJson(tc, "Trees Guide"))
        .andExpect(status().isCreated());
    MarketplaceListing listing = savedListingFor(publisher);

    mockMvc.perform(post("/api/v1/marketplace/listings/{listingId}/import", listing.getId())
        .with(authentication(authFor(importer))))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.sourceListingId").value(listing.getId()))
        .andExpect(jsonPath("$.sourceListingTitle").value("Trees Guide"))
        .andExpect(jsonPath("$.entryCount").value(2))
        .andExpect(jsonPath("$.importedAt").isNotEmpty());

    Long importId = marketplaceImportRepository
        .findByImporterIdOrderByImportedAtDesc(importer.getId())
        .get(0)
        .getId();

    mockMvc.perform(get("/api/v1/marketplace/imports")
        .with(authentication(authFor(importer))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].id").value(importId))
        .andExpect(jsonPath("$[0].sourcePublisherDisplayName").value("Tauzih"));

    mockMvc.perform(get("/api/v1/marketplace/imports/{importId}", importId)
        .with(authentication(authFor(importer))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.entries", hasSize(2)))
        .andExpect(jsonPath("$.entries[0].flashcardQuestion").value("Flashcard question 2"))
        .andExpect(jsonPath("$.entries[0].displayOrder").value(0));

    MarketplaceListing updatedListing = marketplaceListingRepository.findById(listing.getId()).orElseThrow();
    assertEquals(1, updatedListing.getImportCount());
  }

  @Test
  void importListing_enforcesDuplicateVisibilityAndImportOwnershipRules() throws Exception {
    User publisher = userRepository.save(new User("Tauzih", "tauzih@example.com", "hashed"));
    User importer = userRepository.save(new User("Dhruv", "dhruv@example.com", "hashed"));
    User otherUser = userRepository.save(new User("Alex", "alex@example.com", "hashed"));
    CourseModule module = saveModule(publisher, "Trees");
    TC tc = saveTcWithEntries(module, publisher, "Trees", 1);
    publishListing(publisher, publishRequestJson(tc, "Trees Guide"))
        .andExpect(status().isCreated());
    MarketplaceListing listing = savedListingFor(publisher);

    mockMvc.perform(post("/api/v1/marketplace/listings/{listingId}/import", listing.getId())
        .with(authentication(authFor(importer))))
        .andExpect(status().isCreated());

    Long importId = marketplaceImportRepository
        .findByImporterIdOrderByImportedAtDesc(importer.getId())
        .get(0)
        .getId();

    mockMvc.perform(post("/api/v1/marketplace/listings/{listingId}/import", listing.getId())
        .with(authentication(authFor(importer))))
        .andExpect(status().isConflict());

    mockMvc.perform(get("/api/v1/marketplace/imports/{importId}", importId)
        .with(authentication(authFor(otherUser))))
        .andExpect(status().isNotFound());

    listing.unlist(Instant.now());
    marketplaceListingRepository.save(listing);

    mockMvc.perform(post("/api/v1/marketplace/listings/{listingId}/import", listing.getId())
        .with(authentication(authFor(otherUser))))
        .andExpect(status().isNotFound());

    mockMvc.perform(get("/api/v1/marketplace/imports/{importId}", importId)
        .with(authentication(authFor(importer))))
        .andExpect(status().isOk());
  }

  @Test
  void reportListing_allowsOneReportPerUserAndDifferentReporters() throws Exception {
    User publisher = userRepository.save(new User("Tauzih", "tauzih@example.com", "hashed"));
    User firstReporter = userRepository.save(new User("Dhruv", "dhruv@example.com", "hashed"));
    User secondReporter = userRepository.save(new User("Alex", "alex@example.com", "hashed"));
    CourseModule module = saveModule(publisher, "Trees");
    TC tc = saveTcWithEntries(module, publisher, "Trees", 1);
    publishListing(publisher, publishRequestJson(tc, "Trees Guide"))
        .andExpect(status().isCreated());
    MarketplaceListing listing = savedListingFor(publisher);

    reportListing(firstReporter, listing, "INACCURATE_CONTENT")
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.listingId").value(listing.getId()))
        .andExpect(jsonPath("$.reportId").isNumber())
        .andExpect(jsonPath("$.listingStatus").value("PUBLISHED"));

    mockMvc.perform(get("/api/v1/marketplace/listings/{listingId}", listing.getId())
        .with(authentication(authFor(firstReporter))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.hasCurrentUserReported").value(true));

    reportListing(firstReporter, listing, "SPAM")
        .andExpect(status().isConflict());

    reportListing(secondReporter, listing, "SPAM")
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.listingStatus").value("PUBLISHED"));

    assertEquals(2L, marketplaceListingReportRepository.countByListingId(listing.getId()));
  }

  @Test
  void reportListing_atThresholdHidesListingAndBlocksNewImports() throws Exception {
    User publisher = userRepository.save(new User("Tauzih", "tauzih@example.com", "hashed"));
    User firstReporter = userRepository.save(new User("Dhruv", "dhruv@example.com", "hashed"));
    User secondReporter = userRepository.save(new User("Alex", "alex@example.com", "hashed"));
    User thirdReporter = userRepository.save(new User("Sam", "sam@example.com", "hashed"));
    CourseModule module = saveModule(publisher, "Trees");
    TC tc = saveTcWithEntries(module, publisher, "Trees", 1);
    publishListing(publisher, publishRequestJson(tc, "Trees Guide"))
        .andExpect(status().isCreated());
    MarketplaceListing listing = savedListingFor(publisher);

    reportListing(firstReporter, listing, "SPAM").andExpect(status().isCreated());
    reportListing(secondReporter, listing, "SPAM").andExpect(status().isCreated());
    reportListing(thirdReporter, listing, "SPAM")
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.listingStatus").value("UNDER_REVIEW"));

    mockMvc.perform(get("/api/v1/marketplace/listings")
        .with(authentication(authFor(firstReporter))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items", hasSize(0)));

    mockMvc.perform(get("/api/v1/marketplace/listings/{listingId}", listing.getId())
        .with(authentication(authFor(firstReporter))))
        .andExpect(status().isNotFound());

    mockMvc.perform(post("/api/v1/marketplace/listings/{listingId}/import", listing.getId())
        .with(authentication(authFor(firstReporter))))
        .andExpect(status().isNotFound());

    MarketplaceListing hiddenListing = marketplaceListingRepository.findById(listing.getId()).orElseThrow();
    assertEquals(MarketplaceListingStatus.UNDER_REVIEW, hiddenListing.getStatus());
  }

  @Test
  void reportListing_existingImportRemainsReadableAfterThreshold() throws Exception {
    User publisher = userRepository.save(new User("Tauzih", "tauzih@example.com", "hashed"));
    User importer = userRepository.save(new User("Dhruv", "dhruv@example.com", "hashed"));
    User secondReporter = userRepository.save(new User("Alex", "alex@example.com", "hashed"));
    User thirdReporter = userRepository.save(new User("Sam", "sam@example.com", "hashed"));
    CourseModule module = saveModule(publisher, "Trees");
    TC tc = saveTcWithEntries(module, publisher, "Trees", 1);
    publishListing(publisher, publishRequestJson(tc, "Trees Guide"))
        .andExpect(status().isCreated());
    MarketplaceListing listing = savedListingFor(publisher);

    mockMvc.perform(post("/api/v1/marketplace/listings/{listingId}/import", listing.getId())
        .with(authentication(authFor(importer))))
        .andExpect(status().isCreated());

    Long importId = marketplaceImportRepository
        .findByImporterIdOrderByImportedAtDesc(importer.getId())
        .get(0)
        .getId();

    reportListing(importer, listing, "SPAM").andExpect(status().isCreated());
    reportListing(secondReporter, listing, "SPAM").andExpect(status().isCreated());
    reportListing(thirdReporter, listing, "SPAM")
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.listingStatus").value("UNDER_REVIEW"));

    mockMvc.perform(get("/api/v1/marketplace/imports/{importId}", importId)
        .with(authentication(authFor(importer))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.sourceListingId").value(listing.getId()))
        .andExpect(jsonPath("$.entries", hasSize(1)));
  }

  private ResultActions reportListing(User reporter, MarketplaceListing listing, String reason) throws Exception {
    return mockMvc.perform(post("/api/v1/marketplace/listings/{listingId}/reports", listing.getId())
        .with(authentication(authFor(reporter)))
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "reason": "%s",
              "details": "Report details"
            }
            """.formatted(reason)));
  }

  private ResultActions publishListing(User user, String requestJson) throws Exception {
    return mockMvc.perform(post("/api/v1/marketplace/listings")
        .with(authentication(authFor(user)))
        .contentType(MediaType.APPLICATION_JSON)
        .content(requestJson));
  }

  private String publishRequestJson(TC tc, String title) {
    return """
        {
          "tcId": %d,
          "publicTitle": "%s",
          "publisherVisibility": "DISPLAY_NAME"
        }
        """.formatted(tc.getId(), title);
  }

  private MarketplaceListing savedListingFor(User user) {
    MarketplaceListing listingSummary = marketplaceListingRepository
        .findByPublisherIdOrderByPublishedAtDesc(user.getId(), Pageable.unpaged())
        .getContent()
        .get(0);

    return marketplaceListingRepository
        .findByIdAndPublisherId(listingSummary.getId(), user.getId())
        .orElseThrow();
  }

  private CourseModule saveModule(User user, String... topics) {
    CourseModule module = new CourseModule(user, "CS2040", "Year 1 Sem 2", List.of());

    for (String topic : topics) {
      module.addTopic(new ModuleTopic(null, topic));
    }

    return courseModuleRepository.save(module);
  }

  private TC saveTcWithEntries(CourseModule module, User owner, String topic, int entryCount) throws Exception {
    TC tc = new TC(module, owner, topic);
    tc = tcRepository.save(tc);

    for (int index = 1; index <= entryCount; index++) {
      CFC cfc = new CFC(
          module,
          SourceType.TUTORIAL,
          "Tutorial " + index,
          "Title " + index,
          "Summary " + index);

      new CFCEntry(
          cfc,
          (long) index,
          topic,
          "Private question " + index,
          "Private rough note " + index,
          new GeneratedCFCPage(
              "Flashcard question " + index,
              "Flashcard note content " + index));

      cfc = cfcRepository.save(cfc);
      Thread.sleep(5L);
      CFCEntry savedEntry = cfc.getEntries().get(0);
      tc.addEntry(savedEntry);
      cfcEntryRepository.save(savedEntry);
    }

    return tcRepository.findById(tc.getId()).orElseThrow();
  }

  private void saveAdditionalTcEntry(TC tc, CourseModule module, String topic, int index) throws Exception {
    CFC cfc = new CFC(
        module,
        SourceType.TUTORIAL,
        "Tutorial " + index,
        "Title " + index,
        "Summary " + index);

    new CFCEntry(
        cfc,
        (long) index,
        topic,
        "Private question " + index,
        "Private rough note " + index,
        new GeneratedCFCPage(
            "Flashcard question " + index,
            "Flashcard note content " + index));

    cfc = cfcRepository.save(cfc);
    Thread.sleep(5L);
    CFCEntry savedEntry = cfc.getEntries().get(0);
    TC managedTc = tcRepository.findById(tc.getId()).orElseThrow();
    savedEntry.setTc(managedTc);
    cfcEntryRepository.save(savedEntry);
  }

  private UsernamePasswordAuthenticationToken authFor(User user) {
    CustomUserDetails userDetails = new CustomUserDetails(
        user.getId(),
        user.getEmail(),
        user.getUsername(),
        user.getPasswordHash(),
        AuthorityUtils.NO_AUTHORITIES);

    return new UsernamePasswordAuthenticationToken(
        userDetails,
        null,
        userDetails.getAuthorities());
  }
}
