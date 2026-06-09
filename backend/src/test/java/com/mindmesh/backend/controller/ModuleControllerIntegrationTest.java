package com.mindmesh.backend.controller;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.mindmesh.backend.entity.CourseModule;
import com.mindmesh.backend.entity.ModuleTopic;
import com.mindmesh.backend.entity.User;
import com.mindmesh.backend.repository.CFCRepository;
import com.mindmesh.backend.repository.CourseModuleRepository;
import com.mindmesh.backend.repository.UserRepository;
import com.mindmesh.backend.security.CustomUserDetails;

@SpringBootTest
@ActiveProfiles({ "test", "local-ai-fake" })
class ModuleControllerIntegrationTest {

  @Autowired
  private WebApplicationContext webApplicationContext;

  private MockMvc mockMvc;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private CourseModuleRepository courseModuleRepository;

  @Autowired
  private CFCRepository cfcRepository;

  @BeforeEach
  void cleanDatabase() {
    mockMvc = MockMvcBuilders
        .webAppContextSetup(webApplicationContext)
        .apply(org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity())
        .build();

    cfcRepository.deleteAll();
    courseModuleRepository.deleteAll();
    userRepository.deleteAll();
  }

  @Test
  void addModuleTopic_withOwnedModule_returnsAndPersistsUpdatedTopics() throws Exception {
    User user = userRepository.save(new User("Student", "student@example.com", "hashed"));
    CourseModule module = saveModule(user, "Trees");

    mockMvc.perform(post("/api/v1/modules/" + module.getId() + "/topics")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            { "topic": "  Graphs  " }
            """)
        .with(authentication(authFor(user))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.moduleId").value(module.getId()))
        .andExpect(jsonPath("$.courseCode").value("CS2040"))
        .andExpect(jsonPath("$.topics", hasSize(2)))
        .andExpect(jsonPath("$.topics", hasItems("Trees", "Graphs")));

    mockMvc.perform(get("/api/v1/modules/" + module.getId() + "/topics")
        .with(authentication(authFor(user))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.topics", hasSize(2)))
        .andExpect(jsonPath("$.topics", hasItems("Trees", "Graphs")));
  }

  @Test
  void addModuleTopic_withDuplicateIgnoringCase_returnsBadRequest() throws Exception {
    User user = userRepository.save(new User("Student", "student@example.com", "hashed"));
    CourseModule module = saveModule(user, "Trees");

    mockMvc.perform(post("/api/v1/modules/" + module.getId() + "/topics")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            { "topic": "trees" }
            """)
        .with(authentication(authFor(user))))
        .andExpect(status().isBadRequest());
  }

  @Test
  void addModuleTopic_withBlankTopic_returnsBadRequest() throws Exception {
    User user = userRepository.save(new User("Student", "student@example.com", "hashed"));
    CourseModule module = saveModule(user, "Trees");

    mockMvc.perform(post("/api/v1/modules/" + module.getId() + "/topics")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            { "topic": "   " }
            """)
        .with(authentication(authFor(user))))
        .andExpect(status().isBadRequest());
  }

  @Test
  void removeModuleTopic_withOwnedModule_returnsAndPersistsUpdatedTopics() throws Exception {
    User user = userRepository.save(new User("Student", "student@example.com", "hashed"));
    CourseModule module = saveModule(user, "Trees", "Graphs");

    mockMvc.perform(delete("/api/v1/modules/" + module.getId() + "/topics")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            { "topic": "graphs" }
            """)
        .with(authentication(authFor(user))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.topics", hasSize(1)))
        .andExpect(jsonPath("$.topics[0]").value("Trees"));

    mockMvc.perform(get("/api/v1/modules/" + module.getId() + "/topics")
        .with(authentication(authFor(user))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.topics", hasSize(1)))
        .andExpect(jsonPath("$.topics[0]").value("Trees"));
  }

  @Test
  void removeModuleTopic_withMissingTopic_returnsNotFound() throws Exception {
    User user = userRepository.save(new User("Student", "student@example.com", "hashed"));
    CourseModule module = saveModule(user, "Trees");

    mockMvc.perform(delete("/api/v1/modules/" + module.getId() + "/topics")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            { "topic": "Graphs" }
            """)
        .with(authentication(authFor(user))))
        .andExpect(status().isNotFound());
  }

  @Test
  void addModuleTopic_withOtherUsersModule_returnsNotFound() throws Exception {
    User owner = userRepository.save(new User("Owner", "owner@example.com", "hashed"));
    User otherUser = userRepository.save(new User("Other", "other@example.com", "hashed"));
    CourseModule module = saveModule(owner, "Trees");

    mockMvc.perform(post("/api/v1/modules/" + module.getId() + "/topics")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            { "topic": "Graphs" }
            """)
        .with(authentication(authFor(otherUser))))
        .andExpect(status().isNotFound());
  }

  @Test
  void removeModuleTopic_withOtherUsersModule_returnsNotFound() throws Exception {
    User owner = userRepository.save(new User("Owner", "owner@example.com", "hashed"));
    User otherUser = userRepository.save(new User("Other", "other@example.com", "hashed"));
    CourseModule module = saveModule(owner, "Trees");

    mockMvc.perform(delete("/api/v1/modules/" + module.getId() + "/topics")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            { "topic": "Trees" }
            """)
        .with(authentication(authFor(otherUser))))
        .andExpect(status().isNotFound());
  }

  @Test
  void addModuleTopic_withoutAuthentication_isForbidden() throws Exception {
    mockMvc.perform(post("/api/v1/modules/1/topics")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            { "topic": "Graphs" }
            """))
        .andExpect(status().isForbidden());
  }

  @Test
  void removeModuleTopic_withoutAuthentication_isForbidden() throws Exception {
    mockMvc.perform(delete("/api/v1/modules/1/topics")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            { "topic": "Graphs" }
            """))
        .andExpect(status().isForbidden());
  }

  @Test
  void updateModule_withOwnedModule_returnsAndPersistsUpdatedModule() throws Exception {
    User user = userRepository.save(new User("Student", "student@example.com", "hashed"));
    CourseModule module = saveModule(user, "Trees");

    mockMvc.perform(put("/api/v1/modules/" + module.getId())
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "courseCode": " cs2030 ",
              "schoolSem": " Year 2 Sem 1 ",
              "topics": ["OOP", "Streams"]
            }
            """)
        .with(authentication(authFor(user))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(module.getId()))
        .andExpect(jsonPath("$.courseCode").value("CS2030"))
        .andExpect(jsonPath("$.schoolSem").value("Year 2 Sem 1"))
        .andExpect(jsonPath("$.topics", hasSize(2)))
        .andExpect(jsonPath("$.topics", hasItems("OOP", "Streams")));

    mockMvc.perform(get("/api/v1/modules/" + module.getId())
        .with(authentication(authFor(user))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.courseCode").value("CS2030"))
        .andExpect(jsonPath("$.schoolSem").value("Year 2 Sem 1"))
        .andExpect(jsonPath("$.topics", hasSize(2)))
        .andExpect(jsonPath("$.topics", hasItems("OOP", "Streams")));
  }

  @Test
  void updateModule_withDuplicateModuleIdentity_returnsConflict() throws Exception {
    User user = userRepository.save(new User("Student", "student@example.com", "hashed"));
    saveModule(user, "Trees");
    CourseModule module = new CourseModule(user, "MA1521", "Year 1 Sem 1", List.of());
    module.addTopic(new ModuleTopic(null, "Limits"));
    module = courseModuleRepository.save(module);

    mockMvc.perform(put("/api/v1/modules/" + module.getId())
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "courseCode": "CS2040",
              "schoolSem": "Year 1 Sem 2",
              "topics": ["Graphs"]
            }
            """)
        .with(authentication(authFor(user))))
        .andExpect(status().isConflict());
  }

  @Test
  void updateModule_withOtherUsersModule_returnsNotFound() throws Exception {
    User owner = userRepository.save(new User("Owner", "owner@example.com", "hashed"));
    User otherUser = userRepository.save(new User("Other", "other@example.com", "hashed"));
    CourseModule module = saveModule(owner, "Trees");

    mockMvc.perform(put("/api/v1/modules/" + module.getId())
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "courseCode": "CS2030",
              "schoolSem": "Year 2 Sem 1",
              "topics": ["OOP"]
            }
            """)
        .with(authentication(authFor(otherUser))))
        .andExpect(status().isNotFound());
  }

  private CourseModule saveModule(User user, String... topics) {
    CourseModule module = new CourseModule(user, "CS2040", "Year 1 Sem 2", List.of());

    for (String topic : topics) {
      module.addTopic(new ModuleTopic(null, topic));
    }

    return courseModuleRepository.save(module);
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
