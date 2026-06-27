package com.mindmesh.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import com.mindmesh.backend.dto.ai.AIGeneratedCFCEntry;
import com.mindmesh.backend.dto.ai.AIGeneratedCFCResponse;
import com.mindmesh.backend.dto.requests.cfc.CFCHeaderDto;
import com.mindmesh.backend.dto.requests.cfc.CreateCFCRequestDto;
import com.mindmesh.backend.dto.requests.cfc.QnNotePairDto;
import com.mindmesh.backend.dto.responses.cfc.CFCResponseDto;
import com.mindmesh.backend.entity.CFC;
import com.mindmesh.backend.entity.CourseModule;
import com.mindmesh.backend.entity.ModuleTopic;
import com.mindmesh.backend.entity.User;
import com.mindmesh.backend.enums.SourceType;
import com.mindmesh.backend.repository.CFCRepository;
import com.mindmesh.backend.repository.CourseModuleRepository;
import com.mindmesh.backend.service.ai.AICFCGenerationService;

@ExtendWith(MockitoExtension.class)
class CFCServiceTest {

  @Mock
  private CFCRepository cfcRepository;

  @Mock
  private CourseModuleRepository courseModuleRepository;

  @Mock
  private AICFCGenerationService aiCFCGenerationService;

  @Mock
  private TCService tcService;

  @InjectMocks
  private CFCService cfcService;

  private CourseModule module;

  @BeforeEach
  void setUp() {
    User user = new User("Tauzih", "tauzih@example.com", "hashed");
    ReflectionTestUtils.setField(user, "id", 7L);

    module = new CourseModule(user, "CS2040", "Year 1 Sem 2", List.of());
    ReflectionTestUtils.setField(module, "id", 12L);
    module.addTopic(new ModuleTopic(null, "Trees"));
    module.addTopic(new ModuleTopic(null, "Graphs"));
  }

  @Test
  void createCfc_savesParentAndReturnsMappedResponse() {
    CreateCFCRequestDto requestDto = buildRequest(
        12L,
        SourceType.TUTORIAL,
        "Tutorial 5",
        List.of(
            buildItem(1L, "Trees", "  Explain BST deletion  ", List.of(), "  I mixed up predecessor and successor.  "),
            buildItem(2L, "Graphs", "   ", List.of("item_2_img_1"), "Need to revisit BFS traversal.")));

    MockMultipartFile image = new MockMultipartFile(
        "item_2_img_1",
        "graph.png",
        "image/png",
        new byte[] { 1, 2, 3 });

    when(courseModuleRepository.findByIdAndUserId(12L, 7L)).thenReturn(Optional.of(module));
    when(aiCFCGenerationService.generateCFC(any(CourseModule.class), any(CreateCFCRequestDto.class), any()))
        .thenReturn(buildGeneratedAIResponseOutOfOrder());
    when(cfcRepository.save(any(CFC.class))).thenAnswer(invocation -> {
      CFC cfc = invocation.getArgument(0);
      ReflectionTestUtils.setField(cfc, "id", 99L);
      return cfc;
    });

    CFCResponseDto response = cfcService.createCFC(
        requestDto,
        7L,
        Map.of("item_2_img_1", image));

    ArgumentCaptor<CFC> cfcCaptor = ArgumentCaptor.forClass(CFC.class);
    verify(cfcRepository).save(cfcCaptor.capture());

    CFC savedGraph = cfcCaptor.getValue();
    assertEquals(2, savedGraph.getEntries().size());
    assertEquals("AI generated title", savedGraph.getTitle());
    assertEquals("AI generated summary", savedGraph.getSummary());
    assertEquals("CS2040", response.getCourseCode());
    assertEquals("Tutorial 5", response.getSourceTitle());
    assertEquals("AI generated title", response.getTitle());
    assertEquals("AI generated summary", response.getSummary());
    assertEquals(2, response.getEntries().size());
    assertEquals(1L, response.getEntries().get(0).getRequestItemId());
    assertEquals("Explain BST deletion", response.getEntries().get(0).getSourceMaterial().getQuestionText());
    assertEquals("I mixed up predecessor and successor.", response.getEntries().get(0).getSourceMaterial().getRoughNote());
    assertNull(response.getEntries().get(1).getSourceMaterial().getQuestionText());
    assertEquals("AI flashcard question 1", response.getEntries().get(0).getFlashcardQuestion());
    assertEquals("AI flashcard question 2", response.getEntries().get(1).getFlashcardQuestion());
    verify(tcService).syncTCForTopic(module, module.getUser(), "Trees");
    verify(tcService).syncTCForTopic(module, module.getUser(), "Graphs");
  }

  @Test
  void createCfc_rejectsDuplicateItemIds() {
    CreateCFCRequestDto requestDto = buildRequest(
        12L,
        SourceType.TUTORIAL,
        "Tutorial 5",
        List.of(
            buildItem(1L, "Trees", "Question 1", List.of(), "Note 1"),
            buildItem(1L, "Graphs", "Question 2", List.of(), "Note 2")));

    when(courseModuleRepository.findByIdAndUserId(12L, 7L)).thenReturn(Optional.of(module));

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> cfcService.createCFC(requestDto, 7L, Map.of()));

    assertEquals(400, exception.getStatusCode().value());
    assertTrue(exception.getReason().contains("Duplicate item ids"));
    verifyNoInteractions(aiCFCGenerationService);
  }

  @Test
  void createCfc_rejectsTopicOutsideModule() {
    CreateCFCRequestDto requestDto = buildRequest(
        12L,
        SourceType.ASSIGNMENT,
        "Assignment 1",
        List.of(buildItem(1L, "Dynamic Programming", "Question", List.of(), "Note")));

    when(courseModuleRepository.findByIdAndUserId(12L, 7L)).thenReturn(Optional.of(module));

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> cfcService.createCFC(requestDto, 7L, Map.of()));

    assertEquals(400, exception.getStatusCode().value());
    assertTrue(exception.getReason().contains("Topic not found"));
    verifyNoInteractions(aiCFCGenerationService);
  }

  @Test
  void createCfc_rejectsItemWithoutQuestionTextOrImage() {
    CreateCFCRequestDto requestDto = buildRequest(
        12L,
        SourceType.TUTORIAL,
        "Tutorial 5",
        List.of(buildItem(1L, "Trees", "   ", List.of(), "Note")));

    when(courseModuleRepository.findByIdAndUserId(12L, 7L)).thenReturn(Optional.of(module));

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> cfcService.createCFC(requestDto, 7L, Map.of()));

    assertEquals(400, exception.getStatusCode().value());
    assertTrue(exception.getReason().contains("must have a question text or an image"));
    verifyNoInteractions(aiCFCGenerationService);
  }

  @Test
  void createCfc_rejectsNonPngUpload() {
    CreateCFCRequestDto requestDto = buildRequest(
        12L,
        SourceType.TUTORIAL,
        "Tutorial 5",
        List.of(buildItem(1L, "Trees", null, List.of("item_1_img_1"), "Note")));

    MockMultipartFile badFile = new MockMultipartFile(
        "item_1_img_1",
        "tree.jpg",
        "image/jpeg",
        new byte[] { 9, 8, 7 });

    when(courseModuleRepository.findByIdAndUserId(12L, 7L)).thenReturn(Optional.of(module));

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> cfcService.createCFC(requestDto, 7L, Map.of("item_1_img_1", badFile)));

    assertEquals(400, exception.getStatusCode().value());
    assertTrue(exception.getReason().contains("must be a PNG"));
    verifyNoInteractions(aiCFCGenerationService);
  }

  @Test
  void createCfc_rejectsAiOutputMissingSubmittedItem() {
    CreateCFCRequestDto requestDto = buildRequest(
        12L,
        SourceType.TUTORIAL,
        "Tutorial 5",
        List.of(
            buildItem(1L, "Trees", "Question 1", List.of(), "Note 1"),
            buildItem(2L, "Graphs", "Question 2", List.of(), "Note 2")));

    when(courseModuleRepository.findByIdAndUserId(12L, 7L)).thenReturn(Optional.of(module));
    when(aiCFCGenerationService.generateCFC(any(CourseModule.class), any(CreateCFCRequestDto.class), any()))
        .thenReturn(new AIGeneratedCFCResponse(
            "AI generated title",
            "AI generated summary",
            List.of(new AIGeneratedCFCEntry(
                1L,
                "AI flashcard question 1",
                "AI flashcard note content 1"))));

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> cfcService.createCFC(requestDto, 7L, Map.of()));

    assertEquals(502, exception.getStatusCode().value());
    assertTrue(exception.getReason().contains("did not return content for item 2"));
    verify(cfcRepository, never()).save(any(CFC.class));
  }

  private CreateCFCRequestDto buildRequest(
      Long moduleId,
      SourceType sourceType,
      String sourceTitle,
      List<QnNotePairDto> items) {
    CFCHeaderDto headerDto = new CFCHeaderDto();
    headerDto.setSourceType(sourceType);
    headerDto.setSourceTitle(sourceTitle);

    CreateCFCRequestDto requestDto = new CreateCFCRequestDto();
    requestDto.setModuleId(moduleId);
    requestDto.setFlashcardHeader(headerDto);
    requestDto.setItems(items);
    return requestDto;
  }

  private QnNotePairDto buildItem(
      Long itemId,
      String topic,
      String questionText,
      List<String> imageKeys,
      String roughNote) {
    QnNotePairDto item = new QnNotePairDto();
    item.setItemId(itemId);
    item.setTopic(topic);
    item.setQuestionText(questionText);
    item.setImageKeys(imageKeys);
    item.setRoughNote(roughNote);
    return item;
  }

  private AIGeneratedCFCResponse buildGeneratedAIResponseOutOfOrder() {
    return new AIGeneratedCFCResponse(
        "AI generated title",
        "AI generated summary",
        List.of(
            new AIGeneratedCFCEntry(
                2L,
                "AI flashcard question 2",
                "AI flashcard note content 2"),
            new AIGeneratedCFCEntry(
                1L,
                "AI flashcard question 1",
                "AI flashcard note content 1")));
  }
}
