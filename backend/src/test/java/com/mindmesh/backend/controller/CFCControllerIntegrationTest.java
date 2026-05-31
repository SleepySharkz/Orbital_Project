package com.mindmesh.backend.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindmesh.backend.dto.requests.cfc.CFCHeaderDto;
import com.mindmesh.backend.dto.requests.cfc.CreateCFCRequestDto;
import com.mindmesh.backend.dto.requests.cfc.QnNotePairDto;
import com.mindmesh.backend.entity.CFC;
import com.mindmesh.backend.entity.CFCEntry;
import com.mindmesh.backend.entity.CourseModule;
import com.mindmesh.backend.entity.GeneratedCFCPage;
import com.mindmesh.backend.entity.ModuleTopic;
import com.mindmesh.backend.entity.User;
import com.mindmesh.backend.enums.SourceType;
import com.mindmesh.backend.repository.CFCRepository;
import com.mindmesh.backend.repository.CourseModuleRepository;
import com.mindmesh.backend.repository.UserRepository;
import com.mindmesh.backend.security.CustomUserDetails;
import com.mindmesh.backend.service.AICFCGenerationService;
import com.mindmesh.backend.service.AIGeneratedCFCEntry;
import com.mindmesh.backend.service.AIGeneratedCFCResponse;

@SpringBootTest
@ActiveProfiles("test")
class CFCControllerIntegrationTest {

  @TestConfiguration
  static class TestAIConfig {

    @Bean
    AICFCGenerationService aiCFCGenerationService() {
      return (module, requestDto, imageFileMap) -> new AIGeneratedCFCResponse(
          "Test AI Title",
          "Test AI Summary",
          requestDto
              .getItems()
              .stream()
              .map(item -> new AIGeneratedCFCEntry(
                  item.getItemId(),
                  "Test AI learning point " + item.getItemId(),
                  "Test AI explanation " + item.getItemId(),
                  "Test AI mistake pattern " + item.getItemId(),
                  "Test AI review prompt " + item.getItemId()))
              .toList());
    }
  }

  @Autowired
  private WebApplicationContext webApplicationContext;

  private MockMvc mockMvc;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private CourseModuleRepository courseModuleRepository;

  @Autowired
  private CFCRepository cfcRepository;

  private final ObjectMapper objectMapper = new ObjectMapper();

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
  void createCfc_withMultipartRequest_persistsAndReturnsCreatedPayload() throws Exception {
    User user = userRepository.save(new User("Tauzih", "tauzih@example.com", "hashed"));

    CourseModule module = new CourseModule(user, "CS2040", "Year 1 Sem 2", List.of());
    module.addTopic(new ModuleTopic(null, "Trees"));
    module.addTopic(new ModuleTopic(null, "Graphs"));
    module = courseModuleRepository.save(module);

    CreateCFCRequestDto requestDto = buildRequest(module.getId());

    MockMultipartFile requestPart = new MockMultipartFile(
        "request",
        "",
        MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(requestDto));

    MockMultipartFile imagePart = new MockMultipartFile(
        "item_2_img_1",
        "graph.png",
        "image/png",
        new byte[] { 1, 2, 3, 4 });

    CustomUserDetails userDetails = new CustomUserDetails(
        user.getId(),
        user.getEmail(),
        user.getUsername(),
        user.getPasswordHash(),
        AuthorityUtils.NO_AUTHORITIES);

    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
        userDetails,
        null,
        userDetails.getAuthorities());

    mockMvc.perform(multipart("/api/v1/cfcs")
        .file(requestPart)
        .file(imagePart)
        .with(authentication(auth)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.moduleId").value(module.getId()))
        .andExpect(jsonPath("$.courseCode").value("CS2040"))
        .andExpect(jsonPath("$.sourceType").value("TUTORIAL"))
        .andExpect(jsonPath("$.sourceTitle").value("Tutorial 5"))
        .andExpect(jsonPath("$.title").value("Test AI Title"))
        .andExpect(jsonPath("$.summary").value("Test AI Summary"))
        .andExpect(jsonPath("$.entries", hasSize(2)))
        .andExpect(jsonPath("$.entries[0].topic").value("Trees"))
        .andExpect(jsonPath("$.entries[0].sourceMaterial.questionText").value("Explain BST deletion"))
        .andExpect(jsonPath("$.entries[1].sourceMaterial.questionText").doesNotExist())
        .andExpect(jsonPath("$.entries[0].content.learningPoint").value("Test AI learning point 1"))
        .andExpect(jsonPath("$.entries[1].content.learningPoint").value("Test AI learning point 2"));
  }

  @Test
  void createCfc_withoutAuthentication_isForbidden() throws Exception {
    CreateCFCRequestDto requestDto = buildRequest(999L);

    MockMultipartFile requestPart = new MockMultipartFile(
        "request",
        "",
        MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(requestDto));

    mockMvc.perform(multipart("/api/v1/cfcs")
        .file(requestPart))
        .andExpect(status().isForbidden());
  }

  @Test
  void getCfcsForModule_withOwnedModule_returnsSummaries() throws Exception {
    User user = userRepository.save(new User("Tauzih", "tauzih@example.com", "hashed"));
    CourseModule module = saveModule(user);
    CFC cfc = cfcRepository.save(buildCfc(module));

    mockMvc.perform(get("/api/v1/modules/" + module.getId() + "/cfcs")
        .with(authentication(authFor(user))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].id").value(cfc.getId()))
        .andExpect(jsonPath("$[0].moduleId").value(module.getId()))
        .andExpect(jsonPath("$[0].courseCode").value("CS2040"))
        .andExpect(jsonPath("$[0].schoolSem").value("Year 1 Sem 2"))
        .andExpect(jsonPath("$[0].sourceType").value("TUTORIAL"))
        .andExpect(jsonPath("$[0].sourceTitle").value("Tutorial 5"))
        .andExpect(jsonPath("$[0].title").value("AI GEN TITLE PLACEHOLDER"))
        .andExpect(jsonPath("$[0].summary").value("AI GEN SUMMARY PLACEHOLDER"))
        .andExpect(jsonPath("$[0].entries").doesNotExist());
  }

  @Test
  void getCfcsForModule_withOwnedModuleWithoutCfcs_returnsEmptyList() throws Exception {
    User user = userRepository.save(new User("Tauzih", "tauzih@example.com", "hashed"));
    CourseModule module = saveModule(user);

    mockMvc.perform(get("/api/v1/modules/" + module.getId() + "/cfcs")
        .with(authentication(authFor(user))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(0)));
  }

  @Test
  void getCfcsForModule_withOtherUsersModule_returnsNotFound() throws Exception {
    User owner = userRepository.save(new User("Tauzih", "tauzih@example.com", "hashed"));
    User otherUser = userRepository.save(new User("Dhruv", "dhruv@example.com", "hashed"));
    CourseModule module = saveModule(owner);
    cfcRepository.save(buildCfc(module));

    mockMvc.perform(get("/api/v1/modules/" + module.getId() + "/cfcs")
        .with(authentication(authFor(otherUser))))
        .andExpect(status().isNotFound());
  }

  @Test
  void getCfcsForModule_withoutAuthentication_isForbidden() throws Exception {
    mockMvc.perform(get("/api/v1/modules/12/cfcs"))
        .andExpect(status().isForbidden());
  }

  @Test
  void getCfcById_withOwnedCfc_returnsFullCfc() throws Exception {
    User user = userRepository.save(new User("Tauzih", "tauzih@example.com", "hashed"));
    CourseModule module = saveModule(user);
    CFC cfc = cfcRepository.save(buildCfc(module));

    mockMvc.perform(get("/api/v1/cfcs/" + cfc.getId())
        .with(authentication(authFor(user))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(cfc.getId()))
        .andExpect(jsonPath("$.moduleId").value(module.getId()))
        .andExpect(jsonPath("$.courseCode").value("CS2040"))
        .andExpect(jsonPath("$.schoolSem").value("Year 1 Sem 2"))
        .andExpect(jsonPath("$.sourceType").value("TUTORIAL"))
        .andExpect(jsonPath("$.sourceTitle").value("Tutorial 5"))
        .andExpect(jsonPath("$.title").value("AI GEN TITLE PLACEHOLDER"))
        .andExpect(jsonPath("$.summary").value("AI GEN SUMMARY PLACEHOLDER"))
        .andExpect(jsonPath("$.entries", hasSize(1)))
        .andExpect(jsonPath("$.entries[0].topic").value("Trees"))
        .andExpect(jsonPath("$.entries[0].content.learningPoint").value("Placeholder learning point"))
        .andExpect(jsonPath("$.entries[0].content.explanation").value("Placeholder explanation"))
        .andExpect(jsonPath("$.entries[0].sourceMaterial.questionText").value("Explain BST deletion"))
        .andExpect(jsonPath("$.entries[0].sourceMaterial.roughNote").value("I mixed up predecessor and successor."));
  }

  @Test
  void getCfcById_withOtherUsersCfc_returnsNotFound() throws Exception {
    User owner = userRepository.save(new User("Tauzih", "tauzih@example.com", "hashed"));
    User otherUser = userRepository.save(new User("Dhruv", "dhruv@example.com", "hashed"));
    CourseModule module = saveModule(owner);
    CFC cfc = cfcRepository.save(buildCfc(module));

    mockMvc.perform(get("/api/v1/cfcs/" + cfc.getId())
        .with(authentication(authFor(otherUser))))
        .andExpect(status().isNotFound());
  }

  @Test
  void getCfcById_withoutAuthentication_isForbidden() throws Exception {
    mockMvc.perform(get("/api/v1/cfcs/99"))
        .andExpect(status().isForbidden());
  }

  private CreateCFCRequestDto buildRequest(Long moduleId) {
    CFCHeaderDto headerDto = new CFCHeaderDto();
    headerDto.setSourceType(SourceType.TUTORIAL);
    headerDto.setSourceTitle("Tutorial 5");

    QnNotePairDto itemOne = new QnNotePairDto();
    itemOne.setItemId(1L);
    itemOne.setTopic("Trees");
    itemOne.setQuestionText("Explain BST deletion");
    itemOne.setImageKeys(List.of());
    itemOne.setRoughNote("I mixed up predecessor and successor.");

    QnNotePairDto itemTwo = new QnNotePairDto();
    itemTwo.setItemId(2L);
    itemTwo.setTopic("Graphs");
    itemTwo.setQuestionText(null);
    itemTwo.setImageKeys(List.of("item_2_img_1"));
    itemTwo.setRoughNote("Need to revisit BFS traversal.");

    CreateCFCRequestDto requestDto = new CreateCFCRequestDto();
    requestDto.setModuleId(moduleId);
    requestDto.setFlashcardHeader(headerDto);
    requestDto.setItems(List.of(itemOne, itemTwo));
    return requestDto;
  }

  private CourseModule saveModule(User user) {
    CourseModule module = new CourseModule(user, "CS2040", "Year 1 Sem 2", List.of());
    module.addTopic(new ModuleTopic(null, "Trees"));
    module.addTopic(new ModuleTopic(null, "Graphs"));
    return courseModuleRepository.save(module);
  }

  private CFC buildCfc(CourseModule module) {
    CFC cfc = new CFC(
        module,
        SourceType.TUTORIAL,
        "Tutorial 5",
        "AI GEN TITLE PLACEHOLDER",
        "AI GEN SUMMARY PLACEHOLDER");

    new CFCEntry(
        cfc,
        1L,
        "Trees",
        "Explain BST deletion",
        "I mixed up predecessor and successor.",
        new GeneratedCFCPage(
            "Placeholder learning point",
            "Placeholder explanation",
            "Placeholder mistake pattern",
            "Placeholder review prompt"));

    return cfc;
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
