package com.mindmesh.backend.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
import com.mindmesh.backend.entity.Friendship;
import com.mindmesh.backend.entity.GeneratedCFCPage;
import com.mindmesh.backend.entity.ModuleTopic;
import com.mindmesh.backend.entity.SharedTC;
import com.mindmesh.backend.entity.TC;
import com.mindmesh.backend.entity.TCSharingRequest;
import com.mindmesh.backend.entity.User;
import com.mindmesh.backend.enums.SourceType;
import com.mindmesh.backend.enums.TCSharingRequestStatus;
import com.mindmesh.backend.repository.CFCEntryRepository;
import com.mindmesh.backend.repository.CFCRepository;
import com.mindmesh.backend.repository.CourseModuleRepository;
import com.mindmesh.backend.repository.FriendRequestRepository;
import com.mindmesh.backend.repository.FriendshipRepository;
import com.mindmesh.backend.repository.SharedTCRepository;
import com.mindmesh.backend.repository.TCRepository;
import com.mindmesh.backend.repository.TCSharingRequestRepository;
import com.mindmesh.backend.repository.UserRepository;
import com.mindmesh.backend.security.CustomUserDetails;

@SpringBootTest
@ActiveProfiles({ "test", "local-ai-fake" })
class TCSharingRequestControllerIntegrationTest {

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
  private TCSharingRequestRepository tcSharingRequestRepository;

  @Autowired
  private FriendRequestRepository friendRequestRepository;

  @Autowired
  private FriendshipRepository friendshipRepository;

  @Autowired
  private SharedTCRepository sharedTcRepository;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders
        .webAppContextSetup(webApplicationContext)
        .apply(
            org.springframework.security.test.web.servlet.setup
                .SecurityMockMvcConfigurers.springSecurity())
        .build();

    cleanDatabaseState();
  }

  @AfterEach
  void tearDown() {
    cleanDatabaseState();
  }

  @Test
  void sendTcSharingRequest_withFriendAndOwnedTcs_createsSnapshot()
      throws Exception {
    User alice = saveUser("Alice", "alice@example.com");
    User bob = saveUser("Bob", "bob@example.com");
    friendshipRepository.save(new Friendship(alice, bob));
    CourseModule module = saveModule(alice, "Trees", "Graphs");
    TC trees = saveTcWithEntries(module, alice, "Trees", 2);
    TC graphs = saveTcWithEntries(module, alice, "Graphs", 1);

    sendSharingRequest(alice, bob, trees.getId(), graphs.getId())
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.senderUserId").value(alice.getId()))
        .andExpect(jsonPath("$.senderUsername").value("Alice"))
        .andExpect(jsonPath("$.recipientUserId").value(bob.getId()))
        .andExpect(jsonPath("$.recipientUsername").value("Bob"))
        .andExpect(jsonPath("$.status").value("PENDING"))
        .andExpect(jsonPath("$.items", hasSize(2)))
        .andExpect(jsonPath("$.items[0].sourceTcId").value(trees.getId()))
        .andExpect(jsonPath("$.items[0].sourceModuleId").value(module.getId()))
        .andExpect(jsonPath("$.items[0].sourceOwnerUsername").value("Alice"))
        .andExpect(jsonPath("$.items[0].courseCode").value("CS2040"))
        .andExpect(jsonPath("$.items[0].schoolSem").value("Year 1 Sem 2"))
        .andExpect(jsonPath("$.items[0].topic").value("Trees"))
        .andExpect(jsonPath("$.items[0].sourceWasStaleAtSendTime").value(false))
        .andExpect(jsonPath("$.items[0].entryCount").value(2))
        .andExpect(jsonPath("$.items[0].entries[0].flashcardQuestion").value("Flashcard question 2"))
        .andExpect(jsonPath("$.items[0].entries[0].flashcardNoteContent").value("Flashcard note content 2"))
        .andExpect(jsonPath("$.items[0].entries[0].questionText").value("Question 2"))
        .andExpect(jsonPath("$.items[0].entries[0].roughNote").value("Rough note 2"))
        .andExpect(jsonPath("$.items[1].sourceTcId").value(graphs.getId()))
        .andExpect(jsonPath("$.items[1].topic").value("Graphs"));

    assertEquals(1L, tcSharingRequestRepository.count());
  }

  @Test
  void sendTcSharingRequest_withNonFriend_returnsForbidden() throws Exception {
    User alice = saveUser("Alice", "alice@example.com");
    User bob = saveUser("Bob", "bob@example.com");
    CourseModule module = saveModule(alice, "Trees");
    TC tc = saveTcWithEntries(module, alice, "Trees", 1);

    sendSharingRequest(alice, bob, tc.getId())
        .andExpect(status().isForbidden());

    assertEquals(0L, tcSharingRequestRepository.count());
  }

  @Test
  void sendTcSharingRequest_rejectsDuplicateTcIds() throws Exception {
    User alice = saveUser("Alice", "alice@example.com");
    User bob = saveUser("Bob", "bob@example.com");
    friendshipRepository.save(new Friendship(alice, bob));
    CourseModule module = saveModule(alice, "Trees");
    TC tc = saveTcWithEntries(module, alice, "Trees", 1);

    sendSharingRequest(alice, bob, tc.getId(), tc.getId())
        .andExpect(status().isBadRequest());

    assertEquals(0L, tcSharingRequestRepository.count());
  }

  @Test
  void sendTcSharingRequest_withOtherUsersTc_returnsNotFound()
      throws Exception {
    User alice = saveUser("Alice", "alice@example.com");
    User bob = saveUser("Bob", "bob@example.com");
    friendshipRepository.save(new Friendship(alice, bob));
    CourseModule bobModule = saveModule(bob, "Graphs");
    TC bobTc = saveTcWithEntries(bobModule, bob, "Graphs", 1);

    sendSharingRequest(alice, bob, bobTc.getId())
        .andExpect(status().isNotFound());

    assertEquals(0L, tcSharingRequestRepository.count());
  }

  @Test
  void sendTcSharingRequest_rejectsExistingPendingSameDirection()
      throws Exception {
    User alice = saveUser("Alice", "alice@example.com");
    User bob = saveUser("Bob", "bob@example.com");
    friendshipRepository.save(new Friendship(alice, bob));
    CourseModule module = saveModule(alice, "Trees", "Graphs");
    TC firstTc = saveTcWithEntries(module, alice, "Trees", 1);
    TC secondTc = saveTcWithEntries(module, alice, "Graphs", 1);

    sendSharingRequest(alice, bob, firstTc.getId())
        .andExpect(status().isCreated());

    sendSharingRequest(alice, bob, secondTc.getId())
        .andExpect(status().isConflict());

    assertEquals(1L, tcSharingRequestRepository.count());
  }

  @Test
  void requestLists_returnOnlyCurrentUsersPendingSharingRequests()
      throws Exception {
    User alice = saveUser("Alice", "alice@example.com");
    User bob = saveUser("Bob", "bob@example.com");
    User carol = saveUser("Carol", "carol@example.com");
    friendshipRepository.save(new Friendship(alice, bob));
    friendshipRepository.save(new Friendship(alice, carol));
    CourseModule aliceModule = saveModule(alice, "Trees");
    CourseModule carolModule = saveModule(carol, "Graphs");
    TC aliceTc = saveTcWithEntries(aliceModule, alice, "Trees", 1);
    TC carolTc = saveTcWithEntries(carolModule, carol, "Graphs", 1);

    sendSharingRequest(alice, bob, aliceTc.getId())
        .andExpect(status().isCreated());
    sendSharingRequest(carol, alice, carolTc.getId())
        .andExpect(status().isCreated());

    mockMvc.perform(get("/api/v1/tc-sharing-requests/incoming")
        .with(authentication(authFor(alice))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].senderUserId").value(carol.getId()))
        .andExpect(jsonPath("$[0].senderEmail").value("carol@example.com"))
        .andExpect(jsonPath("$[0].recipientUserId").value(alice.getId()))
        .andExpect(jsonPath("$[0].itemCount").value(1))
        .andExpect(jsonPath("$[0].topics[0]").value("Graphs"));

    mockMvc.perform(get("/api/v1/tc-sharing-requests/outgoing")
        .with(authentication(authFor(alice))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].senderUserId").value(alice.getId()))
        .andExpect(jsonPath("$[0].recipientEmail").value("bob@example.com"))
        .andExpect(jsonPath("$[0].itemCount").value(1))
        .andExpect(jsonPath("$[0].topics[0]").value("Trees"));
  }

  @Test
  void getSharingRequestDetail_returnsOnlyForSenderOrRecipient()
      throws Exception {
    User alice = saveUser("Alice", "alice@example.com");
    User bob = saveUser("Bob", "bob@example.com");
    User carol = saveUser("Carol", "carol@example.com");
    friendshipRepository.save(new Friendship(alice, bob));
    CourseModule module = saveModule(alice, "Trees");
    TC tc = saveTcWithEntries(module, alice, "Trees", 1);

    sendSharingRequest(alice, bob, tc.getId())
        .andExpect(status().isCreated());
    TCSharingRequest request = tcSharingRequestRepository.findAll().get(0);

    mockMvc.perform(get("/api/v1/tc-sharing-requests/" + request.getId())
        .with(authentication(authFor(bob))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(request.getId()))
        .andExpect(jsonPath("$.items", hasSize(1)))
        .andExpect(jsonPath("$.items[0].entries", hasSize(1)));

    mockMvc.perform(get("/api/v1/tc-sharing-requests/" + request.getId())
        .with(authentication(authFor(alice))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(request.getId()));

    mockMvc.perform(get("/api/v1/tc-sharing-requests/" + request.getId())
        .with(authentication(authFor(carol))))
        .andExpect(status().isNotFound());
  }

  @Test
  void getSharingRequestDetail_returnsOriginalSnapshotAfterSourceChanges()
      throws Exception {
    User alice = saveUser("Alice", "alice@example.com");
    User bob = saveUser("Bob", "bob@example.com");
    friendshipRepository.save(new Friendship(alice, bob));
    CourseModule module = saveModule(alice, "Trees");
    TC tc = saveTcWithEntries(module, alice, "Trees", 1);

    sendSharingRequest(alice, bob, tc.getId())
        .andExpect(status().isCreated());
    TCSharingRequest request = tcSharingRequestRepository.findAll().get(0);

    CFCEntry sourceEntry = cfcEntryRepository.findAll().get(0);
    sourceEntry.setQuestionText("Changed question");
    sourceEntry.setRoughNote("Changed rough note");
    sourceEntry.getGeneratedCFCPage().updateContent(
        "Changed flashcard question",
        "Changed flashcard note content");
    cfcEntryRepository.save(sourceEntry);

    mockMvc.perform(get("/api/v1/tc-sharing-requests/" + request.getId())
        .with(authentication(authFor(bob))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items[0].entries[0].flashcardQuestion").value("Flashcard question 1"))
        .andExpect(jsonPath("$.items[0].entries[0].flashcardNoteContent").value("Flashcard note content 1"))
        .andExpect(jsonPath("$.items[0].entries[0].questionText").value("Question 1"))
        .andExpect(jsonPath("$.items[0].entries[0].roughNote").value("Rough note 1"));
  }

  @Test
  void tcSharingEndpoints_requireAuthentication()
      throws Exception {
    mockMvc.perform(get("/api/v1/tc-sharing-requests/incoming"))
        .andExpect(status().isForbidden());

    mockMvc.perform(post("/api/v1/friends/1/tc-sharing-requests")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "tcIds": [1]
            }
            """))
        .andExpect(status().isForbidden());

    mockMvc.perform(get("/api/v1/shared-tcs"))
        .andExpect(status().isForbidden());
  }

  @Test
  void detailForRecipient_reportsCanAcceptWhenAllItemsMatchRecipientModules()
      throws Exception {
    User alice = saveUser("Alice", "alice@example.com");
    User bob = saveUser("Bob", "bob@example.com");
    friendshipRepository.save(new Friendship(alice, bob));
    CourseModule aliceModule = saveModule(alice, "Trees", "Graphs");
    TC trees = saveTcWithEntries(aliceModule, alice, "Trees", 1);
    TC graphs = saveTcWithEntries(aliceModule, alice, "Graphs", 1);
    CourseModule bobModule = saveModule(bob, "Trees", "Graphs");

    sendSharingRequest(alice, bob, trees.getId(), graphs.getId())
        .andExpect(status().isCreated());
    TCSharingRequest request = tcSharingRequestRepository.findAll().get(0);

    mockMvc.perform(get("/api/v1/tc-sharing-requests/" + request.getId())
        .with(authentication(authFor(bob))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.canAccept").value(true))
        .andExpect(jsonPath("$.blockingReasons", hasSize(0)))
        .andExpect(jsonPath("$.items[0].compatibilityStatus").value("READY"))
        .andExpect(jsonPath("$.items[0].hasMatchingModule").value(true))
        .andExpect(jsonPath("$.items[0].hasMatchingTopic").value(true))
        .andExpect(jsonPath("$.items[0].matchingRecipientModuleId").value(bobModule.getId()))
        .andExpect(jsonPath("$.items[1].compatibilityStatus").value("READY"))
        .andExpect(jsonPath("$.items[1].hasMatchingModule").value(true))
        .andExpect(jsonPath("$.items[1].hasMatchingTopic").value(true));
  }

  @Test
  void detailForRecipient_reportsMissingModuleBlocker() throws Exception {
    User alice = saveUser("Alice", "alice@example.com");
    User bob = saveUser("Bob", "bob@example.com");
    friendshipRepository.save(new Friendship(alice, bob));
    CourseModule aliceModule = saveModule(alice, "Trees");
    TC trees = saveTcWithEntries(aliceModule, alice, "Trees", 1);

    sendSharingRequest(alice, bob, trees.getId())
        .andExpect(status().isCreated());
    TCSharingRequest request = tcSharingRequestRepository.findAll().get(0);

    mockMvc.perform(get("/api/v1/tc-sharing-requests/" + request.getId())
        .with(authentication(authFor(bob))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.canAccept").value(false))
        .andExpect(jsonPath("$.blockingReasons", hasSize(1)))
        .andExpect(jsonPath("$.blockingReasons[0]").value("Missing module CS2040 / Year 1 Sem 2."))
        .andExpect(jsonPath("$.items[0].compatibilityStatus").value("MISSING_MODULE"))
        .andExpect(jsonPath("$.items[0].hasMatchingModule").value(false))
        .andExpect(jsonPath("$.items[0].hasMatchingTopic").value(false))
        .andExpect(jsonPath("$.items[0].matchingRecipientModuleId").value(nullValue()))
        .andExpect(jsonPath("$.items[0].blockingReason").value("Missing module CS2040 / Year 1 Sem 2."));
  }

  @Test
  void detailForRecipient_reportsMissingTopicBlocker() throws Exception {
    User alice = saveUser("Alice", "alice@example.com");
    User bob = saveUser("Bob", "bob@example.com");
    friendshipRepository.save(new Friendship(alice, bob));
    CourseModule aliceModule = saveModule(alice, "Trees");
    TC trees = saveTcWithEntries(aliceModule, alice, "Trees", 1);
    CourseModule bobModule = saveModule(bob, "Graphs");

    sendSharingRequest(alice, bob, trees.getId())
        .andExpect(status().isCreated());
    TCSharingRequest request = tcSharingRequestRepository.findAll().get(0);

    mockMvc.perform(get("/api/v1/tc-sharing-requests/" + request.getId())
        .with(authentication(authFor(bob))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.canAccept").value(false))
        .andExpect(jsonPath("$.blockingReasons", hasSize(1)))
        .andExpect(jsonPath("$.blockingReasons[0]").value(
            "Module CS2040 / Year 1 Sem 2 exists, but topic Trees is missing."))
        .andExpect(jsonPath("$.items[0].compatibilityStatus").value("MISSING_TOPIC"))
        .andExpect(jsonPath("$.items[0].hasMatchingModule").value(true))
        .andExpect(jsonPath("$.items[0].hasMatchingTopic").value(false))
        .andExpect(jsonPath("$.items[0].matchingRecipientModuleId").value(bobModule.getId()))
        .andExpect(jsonPath("$.items[0].blockingReason").value(
            "Module CS2040 / Year 1 Sem 2 exists, but topic Trees is missing."));
  }

  @Test
  void detailForRecipient_reportsMixedBatchBlockers() throws Exception {
    User alice = saveUser("Alice", "alice@example.com");
    User bob = saveUser("Bob", "bob@example.com");
    friendshipRepository.save(new Friendship(alice, bob));
    CourseModule aliceModule = saveModule(alice, "Trees", "Graphs");
    TC trees = saveTcWithEntries(aliceModule, alice, "Trees", 1);
    TC graphs = saveTcWithEntries(aliceModule, alice, "Graphs", 1);
    saveModule(bob, "Trees");

    sendSharingRequest(alice, bob, trees.getId(), graphs.getId())
        .andExpect(status().isCreated());
    TCSharingRequest request = tcSharingRequestRepository.findAll().get(0);

    mockMvc.perform(get("/api/v1/tc-sharing-requests/" + request.getId())
        .with(authentication(authFor(bob))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.canAccept").value(false))
        .andExpect(jsonPath("$.blockingReasons", hasSize(1)))
        .andExpect(jsonPath("$.items[0].compatibilityStatus").value("READY"))
        .andExpect(jsonPath("$.items[0].blockingReason").value(nullValue()))
        .andExpect(jsonPath("$.items[1].compatibilityStatus").value("MISSING_TOPIC"))
        .andExpect(jsonPath("$.items[1].blockingReason").value(
            "Module CS2040 / Year 1 Sem 2 exists, but topic Graphs is missing."));
  }

  @Test
  void detailForRecipient_recomputesCompatibilityAtReadTime() throws Exception {
    User alice = saveUser("Alice", "alice@example.com");
    User bob = saveUser("Bob", "bob@example.com");
    friendshipRepository.save(new Friendship(alice, bob));
    CourseModule aliceModule = saveModule(alice, "Trees");
    TC trees = saveTcWithEntries(aliceModule, alice, "Trees", 1);

    sendSharingRequest(alice, bob, trees.getId())
        .andExpect(status().isCreated());
    TCSharingRequest request = tcSharingRequestRepository.findAll().get(0);

    mockMvc.perform(get("/api/v1/tc-sharing-requests/" + request.getId())
        .with(authentication(authFor(bob))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.canAccept").value(false))
        .andExpect(jsonPath("$.items[0].compatibilityStatus").value("MISSING_MODULE"));

    saveModule(bob, "Trees");

    mockMvc.perform(get("/api/v1/tc-sharing-requests/" + request.getId())
        .with(authentication(authFor(bob))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.canAccept").value(true))
        .andExpect(jsonPath("$.blockingReasons", hasSize(0)))
        .andExpect(jsonPath("$.items[0].compatibilityStatus").value("READY"));
  }

  @Test
  void detailForSender_doesNotExposeRecipientCompatibility() throws Exception {
    User alice = saveUser("Alice", "alice@example.com");
    User bob = saveUser("Bob", "bob@example.com");
    friendshipRepository.save(new Friendship(alice, bob));
    CourseModule aliceModule = saveModule(alice, "Trees");
    TC trees = saveTcWithEntries(aliceModule, alice, "Trees", 1);

    sendSharingRequest(alice, bob, trees.getId())
        .andExpect(status().isCreated());
    TCSharingRequest request = tcSharingRequestRepository.findAll().get(0);

    mockMvc.perform(get("/api/v1/tc-sharing-requests/" + request.getId())
        .with(authentication(authFor(alice))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.canAccept").value(false))
        .andExpect(jsonPath("$.blockingReasons", hasSize(0)))
        .andExpect(jsonPath("$.items[0].compatibilityStatus").value(nullValue()))
        .andExpect(jsonPath("$.items[0].hasMatchingModule").value(nullValue()))
        .andExpect(jsonPath("$.items[0].hasMatchingTopic").value(nullValue()))
        .andExpect(jsonPath("$.items[0].matchingRecipientModuleId").value(nullValue()))
        .andExpect(jsonPath("$.items[0].blockingReason").value(nullValue()));
  }

  @Test
  void acceptSharingRequest_createsSharedTcCopiesForRecipient()
      throws Exception {
    User alice = saveUser("Alice", "alice@example.com");
    User bob = saveUser("Bob", "bob@example.com");
    friendshipRepository.save(new Friendship(alice, bob));
    CourseModule aliceModule = saveModule(alice, "Trees", "Graphs");
    TC trees = saveTcWithEntries(aliceModule, alice, "Trees", 2);
    TC graphs = saveTcWithEntries(aliceModule, alice, "Graphs", 1);
    saveModule(bob, "Trees", "Graphs");

    sendSharingRequest(alice, bob, trees.getId(), graphs.getId())
        .andExpect(status().isCreated());
    TCSharingRequest request = tcSharingRequestRepository.findAll().get(0);

    acceptSharingRequest(bob, request.getId())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("ACCEPTED"))
        .andExpect(jsonPath("$.respondedAt").isNotEmpty());

    assertEquals(2L, sharedTcRepository.count());
    TCSharingRequest acceptedRequest =
        tcSharingRequestRepository.findById(request.getId()).orElseThrow();
    assertEquals(TCSharingRequestStatus.ACCEPTED, acceptedRequest.getStatus());

    mockMvc.perform(get("/api/v1/shared-tcs")
        .with(authentication(authFor(bob))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].sharedByUsername").value("Alice"))
        .andExpect(jsonPath("$[0].entryCount").isNumber());
  }

  @Test
  void acceptSharingRequest_copiesSnapshotInsteadOfCurrentSourceContent()
      throws Exception {
    User alice = saveUser("Alice", "alice@example.com");
    User bob = saveUser("Bob", "bob@example.com");
    friendshipRepository.save(new Friendship(alice, bob));
    CourseModule aliceModule = saveModule(alice, "Trees");
    TC trees = saveTcWithEntries(aliceModule, alice, "Trees", 1);
    saveModule(bob, "Trees");

    sendSharingRequest(alice, bob, trees.getId())
        .andExpect(status().isCreated());
    TCSharingRequest request = tcSharingRequestRepository.findAll().get(0);

    CFCEntry sourceEntry = cfcEntryRepository.findAll().get(0);
    sourceEntry.setQuestionText("Changed question");
    sourceEntry.setRoughNote("Changed rough note");
    sourceEntry.getGeneratedCFCPage().updateContent(
        "Changed flashcard question",
        "Changed flashcard note content");
    cfcEntryRepository.save(sourceEntry);

    acceptSharingRequest(bob, request.getId())
        .andExpect(status().isOk());

    SharedTC sharedTc = sharedTcRepository.findAll().get(0);

    mockMvc.perform(get("/api/v1/shared-tcs/" + sharedTc.getId())
        .with(authentication(authFor(bob))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.topic").value("Trees"))
        .andExpect(jsonPath("$.sharedByUsername").value("Alice"))
        .andExpect(jsonPath("$.entries", hasSize(1)))
        .andExpect(jsonPath("$.entries[0].flashcardQuestion").value("Flashcard question 1"))
        .andExpect(jsonPath("$.entries[0].flashcardNoteContent").value("Flashcard note content 1"))
        .andExpect(jsonPath("$.entries[0].questionText").value("Question 1"))
        .andExpect(jsonPath("$.entries[0].roughNote").value("Rough note 1"));
  }

  @Test
  void declineSharingRequest_createsNoSharedCopies()
      throws Exception {
    User alice = saveUser("Alice", "alice@example.com");
    User bob = saveUser("Bob", "bob@example.com");
    friendshipRepository.save(new Friendship(alice, bob));
    CourseModule aliceModule = saveModule(alice, "Trees");
    TC trees = saveTcWithEntries(aliceModule, alice, "Trees", 1);

    sendSharingRequest(alice, bob, trees.getId())
        .andExpect(status().isCreated());
    TCSharingRequest request = tcSharingRequestRepository.findAll().get(0);

    declineSharingRequest(bob, request.getId())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("DECLINED"))
        .andExpect(jsonPath("$.respondedAt").isNotEmpty());

    assertEquals(0L, sharedTcRepository.count());
    TCSharingRequest declinedRequest =
        tcSharingRequestRepository.findById(request.getId()).orElseThrow();
    assertEquals(TCSharingRequestStatus.DECLINED, declinedRequest.getStatus());
  }

  @Test
  void nonRecipientCannotAcceptOrDeclineSharingRequest()
      throws Exception {
    User alice = saveUser("Alice", "alice@example.com");
    User bob = saveUser("Bob", "bob@example.com");
    User carol = saveUser("Carol", "carol@example.com");
    friendshipRepository.save(new Friendship(alice, bob));
    CourseModule aliceModule = saveModule(alice, "Trees");
    TC trees = saveTcWithEntries(aliceModule, alice, "Trees", 1);

    sendSharingRequest(alice, bob, trees.getId())
        .andExpect(status().isCreated());
    TCSharingRequest request = tcSharingRequestRepository.findAll().get(0);

    acceptSharingRequest(carol, request.getId())
        .andExpect(status().isNotFound());

    declineSharingRequest(carol, request.getId())
        .andExpect(status().isNotFound());

    assertEquals(0L, sharedTcRepository.count());
  }

  @Test
  void acceptedSharingRequestCannotBeAcceptedAgain()
      throws Exception {
    User alice = saveUser("Alice", "alice@example.com");
    User bob = saveUser("Bob", "bob@example.com");
    friendshipRepository.save(new Friendship(alice, bob));
    CourseModule aliceModule = saveModule(alice, "Trees");
    TC trees = saveTcWithEntries(aliceModule, alice, "Trees", 1);
    saveModule(bob, "Trees");

    sendSharingRequest(alice, bob, trees.getId())
        .andExpect(status().isCreated());
    TCSharingRequest request = tcSharingRequestRepository.findAll().get(0);

    acceptSharingRequest(bob, request.getId())
        .andExpect(status().isOk());

    acceptSharingRequest(bob, request.getId())
        .andExpect(status().isConflict());

    assertEquals(1L, sharedTcRepository.count());
  }

  @Test
  void sourceDeletionBeforeAcceptanceBlocksAccept()
      throws Exception {
    User alice = saveUser("Alice", "alice@example.com");
    User bob = saveUser("Bob", "bob@example.com");
    friendshipRepository.save(new Friendship(alice, bob));
    CourseModule aliceModule = saveModule(alice, "Trees");
    TC trees = saveTcWithEntries(aliceModule, alice, "Trees", 1);
    saveModule(bob, "Trees");

    sendSharingRequest(alice, bob, trees.getId())
        .andExpect(status().isCreated());
    TCSharingRequest request = tcSharingRequestRepository.findAll().get(0);

    for (CFCEntry entry : cfcEntryRepository.findAll()) {
      if (entry.getTc() != null && entry.getTc().getId().equals(trees.getId())) {
        entry.setTc(null);
        cfcEntryRepository.save(entry);
      }
    }
    cfcEntryRepository.flush();
    tcRepository.delete(trees);
    tcRepository.flush();

    acceptSharingRequest(bob, request.getId())
        .andExpect(status().isConflict());

    assertEquals(0L, sharedTcRepository.count());
    TCSharingRequest pendingRequest =
        tcSharingRequestRepository.findById(request.getId()).orElseThrow();
    assertEquals(TCSharingRequestStatus.PENDING, pendingRequest.getStatus());
  }

  @Test
  void acceptedSharedTcsRemainReadableAfterFriendshipRemoval()
      throws Exception {
    User alice = saveUser("Alice", "alice@example.com");
    User bob = saveUser("Bob", "bob@example.com");
    friendshipRepository.save(new Friendship(alice, bob));
    CourseModule aliceModule = saveModule(alice, "Trees");
    TC trees = saveTcWithEntries(aliceModule, alice, "Trees", 1);
    saveModule(bob, "Trees");

    sendSharingRequest(alice, bob, trees.getId())
        .andExpect(status().isCreated());
    TCSharingRequest request = tcSharingRequestRepository.findAll().get(0);
    acceptSharingRequest(bob, request.getId())
        .andExpect(status().isOk());

    SharedTC sharedTc = sharedTcRepository.findAll().get(0);
    friendshipRepository.deleteAll();

    mockMvc.perform(get("/api/v1/shared-tcs/" + sharedTc.getId())
        .with(authentication(authFor(bob))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.topic").value("Trees"))
        .andExpect(jsonPath("$.sharedByUsername").value("Alice"));
  }

  @Test
  void senderCanCancelOwnPendingSharingRequest()
      throws Exception {
    User alice = saveUser("Alice", "alice@example.com");
    User bob = saveUser("Bob", "bob@example.com");
    friendshipRepository.save(new Friendship(alice, bob));
    CourseModule aliceModule = saveModule(alice, "Trees");
    TC trees = saveTcWithEntries(aliceModule, alice, "Trees", 1);

    sendSharingRequest(alice, bob, trees.getId())
        .andExpect(status().isCreated());
    TCSharingRequest request = tcSharingRequestRepository.findAll().get(0);

    cancelSharingRequest(alice, request.getId())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("CANCELLED"))
        .andExpect(jsonPath("$.canAccept").value(false));

    TCSharingRequest cancelledRequest =
        tcSharingRequestRepository.findById(request.getId()).orElseThrow();
    assertEquals(TCSharingRequestStatus.CANCELLED, cancelledRequest.getStatus());

    mockMvc.perform(get("/api/v1/tc-sharing-requests/outgoing")
        .with(authentication(authFor(alice))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(0)));
  }

  @Test
  void nonSenderCannotCancelSharingRequest()
      throws Exception {
    User alice = saveUser("Alice", "alice@example.com");
    User bob = saveUser("Bob", "bob@example.com");
    friendshipRepository.save(new Friendship(alice, bob));
    CourseModule aliceModule = saveModule(alice, "Trees");
    TC trees = saveTcWithEntries(aliceModule, alice, "Trees", 1);

    sendSharingRequest(alice, bob, trees.getId())
        .andExpect(status().isCreated());
    TCSharingRequest request = tcSharingRequestRepository.findAll().get(0);

    cancelSharingRequest(bob, request.getId())
        .andExpect(status().isNotFound());

    TCSharingRequest pendingRequest =
        tcSharingRequestRepository.findById(request.getId()).orElseThrow();
    assertEquals(TCSharingRequestStatus.PENDING, pendingRequest.getStatus());
  }

  @Test
  void cancelledSharingRequestCannotBeAccepted()
      throws Exception {
    User alice = saveUser("Alice", "alice@example.com");
    User bob = saveUser("Bob", "bob@example.com");
    friendshipRepository.save(new Friendship(alice, bob));
    CourseModule aliceModule = saveModule(alice, "Trees");
    TC trees = saveTcWithEntries(aliceModule, alice, "Trees", 1);
    saveModule(bob, "Trees");

    sendSharingRequest(alice, bob, trees.getId())
        .andExpect(status().isCreated());
    TCSharingRequest request = tcSharingRequestRepository.findAll().get(0);

    cancelSharingRequest(alice, request.getId())
        .andExpect(status().isOk());

    acceptSharingRequest(bob, request.getId())
        .andExpect(status().isConflict());

    assertEquals(0L, sharedTcRepository.count());
  }

  @Test
  void removingFriendshipCancelsPendingSharingRequestsInBothDirections()
      throws Exception {
    User alice = saveUser("Alice", "alice@example.com");
    User bob = saveUser("Bob", "bob@example.com");
    friendshipRepository.save(new Friendship(alice, bob));
    CourseModule aliceModule = saveModule(alice, "Trees");
    CourseModule bobModule = saveModule(bob, "Graphs");
    TC aliceTc = saveTcWithEntries(aliceModule, alice, "Trees", 1);
    TC bobTc = saveTcWithEntries(bobModule, bob, "Graphs", 1);

    sendSharingRequest(alice, bob, aliceTc.getId())
        .andExpect(status().isCreated());
    sendSharingRequest(bob, alice, bobTc.getId())
        .andExpect(status().isCreated());

    removeFriend(alice, bob)
        .andExpect(status().isNoContent());

    List<TCSharingRequest> requests = tcSharingRequestRepository.findAll();
    assertEquals(2, requests.size());
    requests.forEach(request ->
        assertEquals(TCSharingRequestStatus.CANCELLED, request.getStatus()));

    mockMvc.perform(get("/api/v1/tc-sharing-requests/incoming")
        .with(authentication(authFor(alice))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(0)));

    mockMvc.perform(get("/api/v1/tc-sharing-requests/outgoing")
        .with(authentication(authFor(alice))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(0)));

    mockMvc.perform(get("/api/v1/tc-sharing-requests/incoming")
        .with(authentication(authFor(bob))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(0)));

    mockMvc.perform(get("/api/v1/tc-sharing-requests/outgoing")
        .with(authentication(authFor(bob))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(0)));
  }

  @Test
  void removingFriendshipDoesNotDeleteAcceptedSharedTcs()
      throws Exception {
    User alice = saveUser("Alice", "alice@example.com");
    User bob = saveUser("Bob", "bob@example.com");
    friendshipRepository.save(new Friendship(alice, bob));
    CourseModule aliceModule = saveModule(alice, "Trees");
    TC trees = saveTcWithEntries(aliceModule, alice, "Trees", 1);
    saveModule(bob, "Trees");

    sendSharingRequest(alice, bob, trees.getId())
        .andExpect(status().isCreated());
    TCSharingRequest request = tcSharingRequestRepository.findAll().get(0);
    acceptSharingRequest(bob, request.getId())
        .andExpect(status().isOk());

    SharedTC sharedTc = sharedTcRepository.findAll().get(0);

    removeFriend(bob, alice)
        .andExpect(status().isNoContent());

    assertEquals(1L, sharedTcRepository.count());
    mockMvc.perform(get("/api/v1/shared-tcs/" + sharedTc.getId())
        .with(authentication(authFor(bob))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.topic").value("Trees"))
        .andExpect(jsonPath("$.sharedByUsername").value("Alice"));
  }

  private void cleanDatabaseState() {
    sharedTcRepository.deleteAll();
    tcSharingRequestRepository.deleteAll();
    cfcRepository.deleteAll();
    tcRepository.deleteAll();
    courseModuleRepository.deleteAll();
    friendRequestRepository.deleteAll();
    friendshipRepository.deleteAll();
    userRepository.deleteAll();
  }

  private User saveUser(String username, String email) {
    return userRepository.save(new User(username, email, "hashed-password"));
  }

  private CourseModule saveModule(User user, String... topics) {
    CourseModule module = new CourseModule(user, "CS2040", "Year 1 Sem 2", List.of());

    for (String topic : topics) {
      module.addTopic(new ModuleTopic(null, topic));
    }

    return courseModuleRepository.save(module);
  }

  private TC saveTcWithEntries(
      CourseModule module,
      User owner,
      String topic,
      int entryCount) throws Exception {
    TC tc = new TC(module, owner, topic);
    tc = tcRepository.save(tc);

    for (int index = 1; index <= entryCount; index++) {
      CFC cfc = new CFC(
          module,
          SourceType.TUTORIAL,
          "Tutorial " + index,
          "Title " + index,
          "Summary " + index);

      CFCEntry entry = new CFCEntry(
          cfc,
          (long) index,
          topic,
          "Question " + index,
          "Rough note " + index,
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

  private ResultActions sendSharingRequest(
      User sender,
      User recipient,
      Long... tcIds) throws Exception {
    return mockMvc.perform(post(
        "/api/v1/friends/" + recipient.getId() + "/tc-sharing-requests")
        .contentType(MediaType.APPLICATION_JSON)
        .content(tcIdsBody(tcIds))
        .with(authentication(authFor(sender))));
  }

  private ResultActions acceptSharingRequest(User recipient, Long requestId)
      throws Exception {
    return mockMvc.perform(post(
        "/api/v1/tc-sharing-requests/" + requestId + "/accept")
        .with(authentication(authFor(recipient))));
  }

  private ResultActions declineSharingRequest(User recipient, Long requestId)
      throws Exception {
    return mockMvc.perform(post(
        "/api/v1/tc-sharing-requests/" + requestId + "/decline")
        .with(authentication(authFor(recipient))));
  }

  private ResultActions cancelSharingRequest(User sender, Long requestId)
      throws Exception {
    return mockMvc.perform(post(
        "/api/v1/tc-sharing-requests/" + requestId + "/cancel")
        .with(authentication(authFor(sender))));
  }

  private ResultActions removeFriend(User currentUser, User friend)
      throws Exception {
    return mockMvc.perform(delete("/api/v1/friends/" + friend.getId())
        .with(authentication(authFor(currentUser))));
  }

  private String tcIdsBody(Long... tcIds) {
    String joinedTcIds = String.join(
        ",",
        java.util.Arrays.stream(tcIds).map(String::valueOf).toList());

    return """
        {
          "tcIds": [%s]
        }
        """.formatted(joinedTcIds);
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
