package com.mindmesh.backend.controller;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

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
import com.mindmesh.backend.entity.TFC;
import com.mindmesh.backend.entity.User;
import com.mindmesh.backend.enums.SourceType;
import com.mindmesh.backend.repository.CFCEntryRepository;
import com.mindmesh.backend.repository.CFCRepository;
import com.mindmesh.backend.repository.CourseModuleRepository;
import com.mindmesh.backend.repository.TFCRepository;
import com.mindmesh.backend.repository.UserRepository;
import com.mindmesh.backend.security.CustomUserDetails;

@SpringBootTest
@ActiveProfiles({ "test", "local-ai-fake" })
class TFCControllerIntegrationTest {

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
  private TFCRepository tfcRepository;

  @BeforeEach
  void cleanDatabase() {
    mockMvc = MockMvcBuilders
        .webAppContextSetup(webApplicationContext)
        .apply(org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity())
        .build();

    cfcRepository.deleteAll();
    tfcRepository.deleteAll();
    courseModuleRepository.deleteAll();
    userRepository.deleteAll();
  }

  @Test
  void getTfcsByModule_withOwnedModule_returnsSummaries() throws Exception {
    User user = userRepository.save(new User("Tauzih", "tauzih@example.com", "hashed"));
    CourseModule module = saveModule(user, "Trees", "Graphs");

    saveTfcWithEntries(module, user, "Trees", 1);
    saveTfcWithEntries(module, user, "Graphs", 1);

    mockMvc.perform(get("/api/v1/modules/" + module.getId() + "/tfcs")
        .with(authentication(authFor(user))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[*].moduleId", hasItems(module.getId().intValue(), module.getId().intValue())))
        .andExpect(jsonPath("$[*].ownerUsername", hasItems("Tauzih", "Tauzih")))
        .andExpect(jsonPath("$[*].courseCode", hasItems("CS2040", "CS2040")))
        .andExpect(jsonPath("$[*].schoolSem", hasItems("Year 1 Sem 2", "Year 1 Sem 2")))
        .andExpect(jsonPath("$[*].topic", hasItems("Trees", "Graphs")))
        .andExpect(jsonPath("$[*].entryCount", hasItems(1, 1)));
  }

  @Test
  void getTfcsByModule_withOtherUsersModule_returnsNotFound() throws Exception {
    User owner = userRepository.save(new User("Tauzih", "tauzih@example.com", "hashed"));
    User otherUser = userRepository.save(new User("Dhruv", "dhruv@example.com", "hashed"));
    CourseModule module = saveModule(owner, "Trees");
    saveTfcWithEntries(module, owner, "Trees", 1);

    mockMvc.perform(get("/api/v1/modules/" + module.getId() + "/tfcs")
        .with(authentication(authFor(otherUser))))
        .andExpect(status().isNotFound());
  }

  @Test
  void getTfcsByModule_withoutAuthentication_isForbidden() throws Exception {
    mockMvc.perform(get("/api/v1/modules/12/tfcs"))
        .andExpect(status().isForbidden());
  }

  @Test
  void getTfcById_withOwnedTfc_returnsDetailWithEntriesNewestFirst() throws Exception {
    User user = userRepository.save(new User("Tauzih", "tauzih@example.com", "hashed"));
    CourseModule module = saveModule(user, "Trees");
    TFC tfc = saveTfcWithEntries(module, user, "Trees", 2);

    mockMvc.perform(get("/api/v1/tfcs/" + tfc.getId())
        .with(authentication(authFor(user))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(tfc.getId()))
        .andExpect(jsonPath("$.moduleId").value(module.getId()))
        .andExpect(jsonPath("$.courseCode").value("CS2040"))
        .andExpect(jsonPath("$.schoolSem").value("Year 1 Sem 2"))
        .andExpect(jsonPath("$.topic").value("Trees"))
        .andExpect(jsonPath("$.entries", hasSize(2)))
        .andExpect(jsonPath("$.entries[0].topic").value("Trees"))
        .andExpect(jsonPath("$.entries[0].flashcardQuestion").value("Flashcard question 2"))
        .andExpect(jsonPath("$.entries[0].flashcardNoteContent").value("Flashcard note content 2"))
        .andExpect(jsonPath("$.entries[0].questionText").value("Question 2"))
        .andExpect(jsonPath("$.entries[0].roughNote").value("Rough note 2"))
        .andExpect(jsonPath("$.entries[1].flashcardQuestion").value("Flashcard question 1"))
        .andExpect(jsonPath("$.entries[1].flashcardNoteContent").value("Flashcard note content 1"))
        .andExpect(jsonPath("$.entries[1].questionText").value("Question 1"))
        .andExpect(jsonPath("$.entries[1].roughNote").value("Rough note 1"));
  }

  @Test
  void getTfcById_withOtherUsersTfc_returnsNotFound() throws Exception {
    User owner = userRepository.save(new User("Tauzih", "tauzih@example.com", "hashed"));
    User otherUser = userRepository.save(new User("Dhruv", "dhruv@example.com", "hashed"));
    CourseModule module = saveModule(owner, "Trees");
    TFC tfc = saveTfcWithEntries(module, owner, "Trees", 1);

    mockMvc.perform(get("/api/v1/tfcs/" + tfc.getId())
        .with(authentication(authFor(otherUser))))
        .andExpect(status().isNotFound());
  }

  @Test
  void getTfcById_withoutAuthentication_isForbidden() throws Exception {
    mockMvc.perform(get("/api/v1/tfcs/99"))
        .andExpect(status().isForbidden());
  }

  private CourseModule saveModule(User user, String... topics) {
    CourseModule module = new CourseModule(user, "CS2040", "Year 1 Sem 2", List.of());

    for (String topic : topics) {
      module.addTopic(new ModuleTopic(null, topic));
    }

    return courseModuleRepository.save(module);
  }

  private TFC saveTfcWithEntries(CourseModule module, User owner, String topic, int entryCount) throws Exception {
    TFC tfc = new TFC(module, owner, topic);
    tfc = tfcRepository.save(tfc);

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
      tfc.addEntry(savedEntry);
      cfcEntryRepository.save(savedEntry);
    }

    return tfcRepository.findById(tfc.getId()).orElseThrow();
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
