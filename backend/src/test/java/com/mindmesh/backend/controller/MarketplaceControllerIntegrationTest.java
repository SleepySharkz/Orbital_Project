package com.mindmesh.backend.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
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
import com.mindmesh.backend.repository.FriendRequestRepository;
import com.mindmesh.backend.repository.FriendshipRepository;
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
  private FriendRequestRepository friendRequestRepository;

  @Autowired
  private FriendshipRepository friendshipRepository;

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
    marketplaceListingRepository.deleteAll();
    friendshipRepository.deleteAll();
    friendRequestRepository.deleteAll();
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
  void publishListing_withAnonymousVisibilityDoesNotExposeUsernameAsDisplayName() throws Exception {
    User user = userRepository.save(new User("Tauzih", "tauzih@example.com", "hashed"));
    CourseModule module = saveModule(user, "Trees");
    TC tc = saveTcWithEntries(module, user, "Trees", 1);

    publishListing(
        user,
        """
            {
              "tcId": %d,
              "publicTitle": "Anonymous Trees",
              "description": "",
              "tags": [],
              "institution": "",
              "publisherVisibility": "ANONYMOUS"
            }
            """.formatted(tc.getId()))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.status").value("PUBLISHED"));

    MarketplaceListing listing = savedListingFor(user);
    assertEquals(PublisherVisibility.ANONYMOUS, listing.getPublisherVisibility());
    assertEquals("Anonymous", listing.getPublisherDisplayName());
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
  void publishListing_withStaleTc_returnsBadRequest() throws Exception {
    User user = userRepository.save(new User("Tauzih", "tauzih@example.com", "hashed"));
    CourseModule module = saveModule(user, "Trees");
    TC tc = saveTcWithEntries(module, user, "Trees", 1);
    module.removeTopic(module.getTopics().get(0));
    courseModuleRepository.save(module);

    publishListing(
        user,
        publishRequestJson(tc, "Stale Trees"))
        .andExpect(status().isBadRequest());

    assertEquals(0L, marketplaceListingRepository.count());
  }

  @Test
  void publishListing_withDuplicatePublishedListing_returnsConflict() throws Exception {
    User user = userRepository.save(new User("Tauzih", "tauzih@example.com", "hashed"));
    CourseModule module = saveModule(user, "Trees");
    TC tc = saveTcWithEntries(module, user, "Trees", 1);

    publishListing(user, publishRequestJson(tc, "First Listing"))
        .andExpect(status().isCreated());

    publishListing(user, publishRequestJson(tc, "Second Listing"))
        .andExpect(status().isConflict());

    assertEquals(1L, marketplaceListingRepository.count());
  }

  @Test
  void publishListing_withInvalidBody_returnsBadRequest() throws Exception {
    User user = userRepository.save(new User("Tauzih", "tauzih@example.com", "hashed"));

    publishListing(
        user,
        """
            {
              "tcId": -1,
              "publicTitle": "No",
              "publisherVisibility": null
            }
            """)
        .andExpect(status().isBadRequest());
  }

  @Test
  void publishListing_withoutAuthentication_isForbidden() throws Exception {
    mockMvc.perform(post("/api/v1/marketplace/listings")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "tcId": 1,
              "publicTitle": "BST Revision Pack",
              "publisherVisibility": "DISPLAY_NAME"
            }
            """))
        .andExpect(status().isForbidden());
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
        .findByPublisherIdOrderByPublishedAtDesc(user.getId(), PageRequest.of(0, 1))
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
