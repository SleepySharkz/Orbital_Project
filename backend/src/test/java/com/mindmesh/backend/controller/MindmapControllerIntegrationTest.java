package com.mindmesh.backend.controller;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.mindmesh.backend.entity.CFC;
import com.mindmesh.backend.entity.CFCEntry;
import com.mindmesh.backend.entity.CourseModule;
import com.mindmesh.backend.entity.GeneratedCFCPage;
import com.mindmesh.backend.entity.ModuleTopic;
import com.mindmesh.backend.entity.SharedTC;
import com.mindmesh.backend.entity.SharedTCEntry;
import com.mindmesh.backend.entity.TC;
import com.mindmesh.backend.entity.TCInsight;
import com.mindmesh.backend.entity.TCInsightPoint;
import com.mindmesh.backend.entity.TCSharingRequest;
import com.mindmesh.backend.entity.TCSharingRequestItem;
import com.mindmesh.backend.entity.User;
import com.mindmesh.backend.enums.SourceType;
import com.mindmesh.backend.repository.CFCEntryRepository;
import com.mindmesh.backend.repository.CFCRepository;
import com.mindmesh.backend.repository.CourseModuleRepository;
import com.mindmesh.backend.repository.FriendRequestRepository;
import com.mindmesh.backend.repository.FriendshipRepository;
import com.mindmesh.backend.repository.SharedTCRepository;
import com.mindmesh.backend.repository.TCInsightRepository;
import com.mindmesh.backend.repository.TCRepository;
import com.mindmesh.backend.repository.TCSharingRequestRepository;
import com.mindmesh.backend.repository.UserRepository;
import com.mindmesh.backend.security.CustomUserDetails;

@SpringBootTest
@ActiveProfiles({ "test", "local-ai-fake" })
class MindmapControllerIntegrationTest {

  @Autowired
  private WebApplicationContext webApplicationContext;

  private MockMvc mockMvc;

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
  private TCInsightRepository tcInsightRepository;

  @Autowired
  private TCSharingRequestRepository tcSharingRequestRepository;

  @Autowired
  private SharedTCRepository sharedTcRepository;

  @Autowired
  private FriendRequestRepository friendRequestRepository;

  @Autowired
  private FriendshipRepository friendshipRepository;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders
        .webAppContextSetup(webApplicationContext)
        .apply(org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity())
        .build();
    cleanDatabase();
  }

  @AfterEach
  void cleanDatabaseAfterTest() {
    cleanDatabase();
  }

  @Test
  void getMindmap_returnsOnlyActiveOwnedNonEmptyTcsAndExcludesSharedTc() throws Exception {
    User owner = userRepository.save(new User("Owner", "owner@example.com", "hashed"));
    User otherUser = userRepository.save(new User("Other", "other@example.com", "hashed"));
    CourseModule module = saveModule(owner, "Trees", "Graphs", "Empty", "Queues");

    TC treesTc = saveTcWithEntry(module, owner, "Trees", 1L);
    saveTcWithEntry(module, owner, "Removed Topic", 2L);
    tcRepository.save(new TC(module, owner, "Empty"));
    saveTcWithEntry(module, otherUser, "Queues", 3L);
    saveSharedTc(module, owner, otherUser, "Graphs");

    mockMvc.perform(get("/api/v1/modules/" + module.getId() + "/mindmap")
        .with(authentication(authFor(owner))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.moduleId").value(module.getId()))
        .andExpect(jsonPath("$.courseCode").value("CS2040S"))
        .andExpect(jsonPath("$.schoolSem").value("Year1Sem2"))
        .andExpect(jsonPath("$.nodes", hasSize(1)))
        .andExpect(jsonPath("$.nodes[0].tcId").value(treesTc.getId()))
        .andExpect(jsonPath("$.nodes[0].topic").value("Trees"))
        .andExpect(jsonPath("$.nodes[0].entryCount").value(1))
        .andExpect(jsonPath("$.edges", hasSize(0)));
  }

  @Test
  void getMindmap_returnsReadyAndReadableRefreshingEdgesOnly() throws Exception {
    User owner = userRepository.save(new User("Owner", "owner@example.com", "hashed"));
    CourseModule module = saveModule(owner, "Trees", "Graphs", "Hashing", "Sorting");
    TC treesTc = saveTcWithEntry(module, owner, "Trees", 10L);
    TC graphsTc = saveTcWithEntry(module, owner, "Graphs", 11L);
    TC hashingTc = saveTcWithEntry(module, owner, "Hashing", 12L);
    TC sortingTc = saveTcWithEntry(module, owner, "Sorting", 14L);
    TC staleTc = saveTcWithEntry(module, owner, "Removed Topic", 13L);

    saveReadyInsight(owner, module, treesTc, graphsTc, "Trees and graphs");

    TCInsight refreshing = saveReadyInsight(
        owner,
        module,
        graphsTc,
        hashingTc,
        "Graph lookup with hashing");
    refreshing.markRefreshing(Instant.parse("2026-07-15T10:10:00Z"));
    tcInsightRepository.saveAndFlush(refreshing);

    TCInsight noLink = new TCInsight(owner, module, treesTc, hashingTc);
    noLink.markNoUsefulLink(
        "No concrete relationship is supported by the current notes.",
        "trees-hash",
        "hashing-hash",
        Instant.parse("2026-07-15T10:20:00Z"));
    tcInsightRepository.saveAndFlush(noLink);

    tcInsightRepository.saveAndFlush(
        new TCInsight(owner, module, treesTc, sortingTc));

    TCInsight refreshFailed = saveReadyInsight(
        owner,
        module,
        graphsTc,
        sortingTc,
        "Readable but failed refresh");
    refreshFailed.markRefreshFailed(Instant.parse("2026-07-15T10:25:00Z"));
    tcInsightRepository.saveAndFlush(refreshFailed);

    saveReadyInsight(owner, module, treesTc, staleTc, "Inactive edge");

    mockMvc.perform(get("/api/v1/modules/" + module.getId() + "/mindmap")
        .with(authentication(authFor(owner))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.nodes", hasSize(4)))
        .andExpect(jsonPath(
            "$.nodes[*].topic",
            hasItems("Trees", "Graphs", "Hashing", "Sorting")))
        .andExpect(jsonPath("$.edges", hasSize(3)))
        .andExpect(jsonPath(
            "$.edges[*].title",
            hasItems(
                "Trees and graphs",
                "Graph lookup with hashing",
                "Readable but failed refresh")))
        .andExpect(jsonPath(
            "$.edges[*].status",
            hasItems("READY", "REFRESHING", "REFRESH_FAILED")))
        .andExpect(jsonPath("$.edges[*].isRefreshing", hasItems(false, true)));
  }

  @Test
  void getMindmap_withOtherUsersModule_returnsNotFound() throws Exception {
    User owner = userRepository.save(new User("Owner", "owner@example.com", "hashed"));
    User otherUser = userRepository.save(new User("Other", "other@example.com", "hashed"));
    CourseModule module = saveModule(owner, "Trees");

    mockMvc.perform(get("/api/v1/modules/" + module.getId() + "/mindmap")
        .with(authentication(authFor(otherUser))))
        .andExpect(status().isNotFound());
  }

  @Test
  void getMindmap_withoutAuthentication_isForbidden() throws Exception {
    mockMvc.perform(get("/api/v1/modules/1/mindmap"))
        .andExpect(status().isForbidden());
  }

  private TCInsight saveReadyInsight(
      User owner,
      CourseModule module,
      TC firstTc,
      TC secondTc,
      String title) {
    TCInsight insight = new TCInsight(owner, module, firstTc, secondTc);
    insight.markReady(
        title,
        "A saved explanation connecting both topic sheets.",
        List.of(new TCInsightPoint(
            "Connection",
            "A concrete relationship between the two topics.",
            "Topic A notes",
            "Topic B notes",
            0)),
        "hash-a",
        "hash-b",
        Instant.parse("2026-07-15T10:00:00Z"));
    return tcInsightRepository.saveAndFlush(insight);
  }

  private CourseModule saveModule(User owner, String... topics) {
    CourseModule module = new CourseModule(owner, "CS2040S", "Year1Sem2", List.of());
    for (String topic : topics) {
      module.addTopic(new ModuleTopic(null, topic));
    }
    return courseModuleRepository.save(module);
  }

  private TC saveTcWithEntry(
      CourseModule module,
      User owner,
      String topic,
      Long requestItemId) {
    TC tc = tcRepository.save(new TC(module, owner, topic));
    CFC cfc = new CFC(
        module,
        SourceType.TUTORIAL,
        "Tutorial " + requestItemId,
        topic + " revision",
        "Summary");
    CFCEntry entry = new CFCEntry(
        cfc,
        requestItemId,
        topic,
        "Question " + requestItemId,
        "Rough note " + requestItemId,
        new GeneratedCFCPage(
            "Flashcard question " + requestItemId,
            "Flashcard note " + requestItemId));
    cfcRepository.save(cfc);
    tc.addEntry(entry);
    cfcEntryRepository.save(entry);
    return tcRepository.findById(tc.getId()).orElseThrow();
  }

  private void saveSharedTc(
      CourseModule recipientModule,
      User recipient,
      User originalOwner,
      String topic) {
    TCSharingRequest request = new TCSharingRequest(originalOwner, recipient);
    new TCSharingRequestItem(
        request,
        0,
        900L,
        recipientModule.getId(),
        originalOwner.getUsername(),
        recipientModule.getCourseCode(),
        recipientModule.getSchoolSem(),
        topic,
        false,
        null);
    request.accept(Instant.parse("2026-07-15T09:00:00Z"));
    request = tcSharingRequestRepository.save(request);

    Long sourceItemId = request.getItems().get(0).getId();
    SharedTC sharedTc = new SharedTC(
        recipient,
        originalOwner,
        request,
        sourceItemId,
        recipientModule,
        recipientModule.getCourseCode(),
        recipientModule.getSchoolSem(),
        topic,
        originalOwner.getUsername(),
        Instant.parse("2026-07-15T09:00:00Z"));
    new SharedTCEntry(
        sharedTc,
        0,
        901L,
        "Shared question",
        "Shared note",
        "Shared source question",
        "Shared rough note",
        SourceType.TUTORIAL,
        "Tutorial fixture",
        null);
    sharedTcRepository.save(sharedTc);
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

  private void cleanDatabase() {
    tcInsightRepository.deleteAll();
    sharedTcRepository.deleteAll();
    tcSharingRequestRepository.deleteAll();
    cfcRepository.deleteAll();
    tcRepository.deleteAll();
    courseModuleRepository.deleteAll();
    friendRequestRepository.deleteAll();
    friendshipRepository.deleteAll();
    userRepository.deleteAll();
  }
}
