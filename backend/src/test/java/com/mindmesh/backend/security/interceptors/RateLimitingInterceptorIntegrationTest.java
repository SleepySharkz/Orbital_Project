package com.mindmesh.backend.security.interceptors;

import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindmesh.backend.dto.ai.AIGeneratedCFCEntry;
import com.mindmesh.backend.dto.ai.AIGeneratedCFCResponse;
import com.mindmesh.backend.dto.requests.cfc.CFCHeaderDto;
import com.mindmesh.backend.dto.requests.cfc.CreateCFCRequestDto;
import com.mindmesh.backend.dto.requests.cfc.QnNotePairDto;
import com.mindmesh.backend.entity.CourseModule;
import com.mindmesh.backend.entity.ModuleTopic;
import com.mindmesh.backend.entity.User;
import com.mindmesh.backend.enums.SourceType;
import com.mindmesh.backend.repository.CFCRepository;
import com.mindmesh.backend.repository.CourseModuleRepository;
import com.mindmesh.backend.repository.FriendRequestRepository;
import com.mindmesh.backend.repository.FriendshipRepository;
import com.mindmesh.backend.repository.UserRepository;
import com.mindmesh.backend.security.CustomUserDetails;
import com.mindmesh.backend.service.ai.AICFCGenerationService;

@SpringBootTest
@ActiveProfiles("test")
class RateLimitingInterceptorIntegrationTest {

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
                  "Test AI flashcard question " + item.getItemId(),
                  "Test AI flashcard note content " + item.getItemId()))
              .toList());
    }
  }

  @Autowired
  private WebApplicationContext webApplicationContext;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private CourseModuleRepository courseModuleRepository;

  @Autowired
  private CFCRepository cfcRepository;

  @Autowired
  private FriendRequestRepository friendRequestRepository;

  @Autowired
  private FriendshipRepository friendshipRepository;

  @Autowired
  private SlidingWindowRateLimiterService rateLimiterService;

  private final ObjectMapper objectMapper = new ObjectMapper();

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders
        .webAppContextSetup(webApplicationContext)
        .apply(org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity())
        .build();

    cfcRepository.deleteAll();
    courseModuleRepository.deleteAll();
    friendRequestRepository.deleteAll();
    friendshipRepository.deleteAll();
    userRepository.deleteAll();

    @SuppressWarnings("unchecked")
    ConcurrentHashMap<String, ConcurrentLinkedDeque<Instant>> requestLog = (ConcurrentHashMap<String, ConcurrentLinkedDeque<Instant>>) ReflectionTestUtils
        .getField(
            rateLimiterService,
            "requestLog");
    requestLog.clear();
  }

  @Test
  void createCfc_returns429_whenRateLimitBucketAlreadyFull() throws Exception {
    User user = userRepository.save(new User("Tauzih", "tauzih@example.com", "hashed"));

    CourseModule module = new CourseModule(user, "CS2040", "Year 1 Sem 2", List.of());
    module.addTopic(new ModuleTopic(null, "Trees"));
    module = courseModuleRepository.save(module);

    CreateCFCRequestDto requestDto = buildRequest(module.getId());

    MockMultipartFile requestPart = new MockMultipartFile(
        "request",
        "",
        MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(requestDto));

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

    String key = "user:" + user.getId() + ":POST:/api/v1/cfcs";

    @SuppressWarnings("unchecked")
    ConcurrentHashMap<String, ConcurrentLinkedDeque<Instant>> requestLog = (ConcurrentHashMap<String, ConcurrentLinkedDeque<Instant>>) ReflectionTestUtils
        .getField(
            rateLimiterService,
            "requestLog");

    ConcurrentLinkedDeque<Instant> timestamps = new ConcurrentLinkedDeque<>();
    for (int i = 0; i < 5; i++) {
      timestamps.add(Instant.now());
    }
    requestLog.put(key, timestamps);

    mockMvc.perform(multipart("/api/v1/cfcs")
        .file(requestPart)
        .with(authentication(auth)))
        .andExpect(status().isTooManyRequests())
        .andExpect(jsonPath("$.error", is("Too many requests")));
  }

  private CreateCFCRequestDto buildRequest(Long moduleId) {
    CFCHeaderDto headerDto = new CFCHeaderDto();
    headerDto.setSourceType(SourceType.TUTORIAL);
    headerDto.setSourceTitle("Tutorial 5");

    QnNotePairDto item = new QnNotePairDto();
    item.setItemId(1L);
    item.setTopic("Trees");
    item.setQuestionText("Explain BST deletion");
    item.setImageKeys(List.of());
    item.setRoughNote("I mixed up predecessor and successor.");

    CreateCFCRequestDto requestDto = new CreateCFCRequestDto();
    requestDto.setModuleId(moduleId);
    requestDto.setFlashcardHeader(headerDto);
    requestDto.setItems(List.of(item));
    return requestDto;
  }
}
