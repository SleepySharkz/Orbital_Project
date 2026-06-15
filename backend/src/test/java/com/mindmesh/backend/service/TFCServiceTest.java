package com.mindmesh.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

import com.mindmesh.backend.dto.responses.tfc.TfcContentResponse;
import com.mindmesh.backend.dto.responses.tfc.TfcSummaryResponse;
import com.mindmesh.backend.entity.CFC;
import com.mindmesh.backend.entity.CFCEntry;
import com.mindmesh.backend.entity.CourseModule;
import com.mindmesh.backend.entity.GeneratedCFCPage;
import com.mindmesh.backend.entity.ModuleTopic;
import com.mindmesh.backend.entity.TFC;
import com.mindmesh.backend.entity.User;
import com.mindmesh.backend.enums.SourceType;
import com.mindmesh.backend.repository.CFCEntryRepository;
import com.mindmesh.backend.repository.CourseModuleRepository;
import com.mindmesh.backend.repository.TFCRepository;

@ExtendWith(MockitoExtension.class)
class TFCServiceTest {

  @Mock
  private TFCRepository tfcRepository;

  @Mock
  private CFCEntryRepository cfcEntryRepository;

  @Mock
  private CourseModuleRepository courseModuleRepository;

  @InjectMocks
  private TFCService tfcService;

  private User owner;
  private CourseModule module;

  @BeforeEach
  void setUp() {
    owner = new User("Tauzih", "tauzih@example.com", "hashed");
    ReflectionTestUtils.setField(owner, "id", 7L);

    module = new CourseModule(owner, "CS2040", "Year 1 Sem 2", List.of());
    ReflectionTestUtils.setField(module, "id", 12L);
    module.addTopic(new ModuleTopic(null, "Trees"));
    module.addTopic(new ModuleTopic(null, "Graphs"));
  }

  @Test
  void syncTfcForTopic_createsNewTfcAndAttachesMatchingEntries() {
    CFCEntry treeEntryOne = buildEntry(101L, "Trees");
    CFCEntry treeEntryTwo = buildEntry(102L, "Trees");

    when(tfcRepository.findByOwnerIdAndModuleIdAndTopic(7L, 12L, "Trees")).thenReturn(Optional.empty());
    when(cfcEntryRepository.findAllByCfcModuleUserIdAndCfcModuleIdAndTopic(7L, 12L, "Trees"))
        .thenReturn(List.of(treeEntryOne, treeEntryTwo));
    when(tfcRepository.save(any(TFC.class))).thenAnswer(invocation -> invocation.getArgument(0));

    tfcService.syncTFCForTopic(module, owner, "Trees");

    ArgumentCaptor<TFC> tfcCaptor = ArgumentCaptor.forClass(TFC.class);
    verify(tfcRepository).save(tfcCaptor.capture());

    TFC savedTfc = tfcCaptor.getValue();
    assertSame(module, savedTfc.getModule());
    assertSame(owner, savedTfc.getOwner());
    assertEquals("Trees", savedTfc.getTopic());
    assertEquals(2, savedTfc.getEntries().size());
    assertTrue(savedTfc.getEntries().contains(treeEntryOne));
    assertTrue(savedTfc.getEntries().contains(treeEntryTwo));
    assertSame(savedTfc, treeEntryOne.getTfc());
    assertSame(savedTfc, treeEntryTwo.getTfc());
  }

  @Test
  void syncTfcForTopic_replacesMembershipByRemovingStaleAndAddingMissingEntries() {
    TFC existingTfc = new TFC(module, owner, "Trees");
    ReflectionTestUtils.setField(existingTfc, "id", 55L);

    CFCEntry keepEntry = buildEntry(201L, "Trees");
    CFCEntry staleEntry = buildEntry(202L, "Trees");
    CFCEntry newEntry = buildEntry(203L, "Trees");

    existingTfc.addEntry(keepEntry);
    existingTfc.addEntry(staleEntry);

    when(tfcRepository.findByOwnerIdAndModuleIdAndTopic(7L, 12L, "Trees")).thenReturn(Optional.of(existingTfc));
    when(cfcEntryRepository.findAllByCfcModuleUserIdAndCfcModuleIdAndTopic(7L, 12L, "Trees"))
        .thenReturn(List.of(keepEntry, newEntry));
    when(tfcRepository.save(any(TFC.class))).thenAnswer(invocation -> invocation.getArgument(0));

    tfcService.syncTFCForTopic(module, owner, "Trees");

    assertEquals(2, existingTfc.getEntries().size());
    assertTrue(existingTfc.getEntries().contains(keepEntry));
    assertTrue(existingTfc.getEntries().contains(newEntry));
    assertTrue(!existingTfc.getEntries().contains(staleEntry));
    assertSame(existingTfc, keepEntry.getTfc());
    assertSame(existingTfc, newEntry.getTfc());
    assertNull(staleEntry.getTfc());
    verify(tfcRepository).save(existingTfc);
  }

  @Test
  void syncTfcForTopic_deletesExistingTfcWhenNoMatchingEntriesRemain() {
    TFC existingTfc = new TFC(module, owner, "Trees");
    ReflectionTestUtils.setField(existingTfc, "id", 88L);

    when(tfcRepository.findByOwnerIdAndModuleIdAndTopic(7L, 12L, "Trees")).thenReturn(Optional.of(existingTfc));
    when(cfcEntryRepository.findAllByCfcModuleUserIdAndCfcModuleIdAndTopic(7L, 12L, "Trees"))
        .thenReturn(List.of());

    tfcService.syncTFCForTopic(module, owner, "Trees");

    verify(tfcRepository).delete(existingTfc);
    verify(tfcRepository, never()).save(any(TFC.class));
  }

  @Test
  void syncTfcForTopic_doesNothingWhenNoExistingTfcAndNoMatchingEntries() {
    when(tfcRepository.findByOwnerIdAndModuleIdAndTopic(7L, 12L, "Trees")).thenReturn(Optional.empty());
    when(cfcEntryRepository.findAllByCfcModuleUserIdAndCfcModuleIdAndTopic(7L, 12L, "Trees"))
        .thenReturn(List.of());

    tfcService.syncTFCForTopic(module, owner, "Trees");

    verify(tfcRepository, never()).delete(any(TFC.class));
    verify(tfcRepository, never()).save(any(TFC.class));
  }

  @Test
  void getTfcsByModule_returnsOwnedTfcsSortedByUpdatedAtDesc() {
    TFC newerTfc = new TFC(module, owner, "Graphs");
    ReflectionTestUtils.setField(newerTfc, "id", 71L);
    ReflectionTestUtils.setField(newerTfc, "updatedAt", java.time.LocalDateTime.of(2026, 6, 15, 12, 0));

    TFC olderTfc = new TFC(module, owner, "Trees");
    ReflectionTestUtils.setField(olderTfc, "id", 70L);
    ReflectionTestUtils.setField(olderTfc, "updatedAt", java.time.LocalDateTime.of(2026, 6, 14, 12, 0));

    olderTfc.addEntry(buildEntry(301L, "Trees"));
    olderTfc.addEntry(buildEntry(302L, "Trees"));
    newerTfc.addEntry(buildEntry(303L, "Graphs"));

    when(courseModuleRepository.findByIdAndUserId(12L, 7L)).thenReturn(Optional.of(module));
    when(tfcRepository.findAllByOwnerIdAndModuleIdOrderByUpdatedAtDesc(7L, 12L))
        .thenReturn(List.of(newerTfc, olderTfc));

    List<TfcSummaryResponse> responses = tfcService.getTFCsByModule(12L, 7L);

    assertEquals(2, responses.size());
    assertEquals(71L, responses.get(0).getId());
    assertEquals("Graphs", responses.get(0).getTopic());
    assertEquals("Tauzih", responses.get(0).getOwnerUsername());
    assertEquals(1, responses.get(0).getEntryCount());
    assertEquals(70L, responses.get(1).getId());
    assertEquals("Trees", responses.get(1).getTopic());
    assertEquals(2, responses.get(1).getEntryCount());
  }

  @Test
  void getTfcsByModule_withUnownedModule_throwsNotFound() {
    when(courseModuleRepository.findByIdAndUserId(12L, 7L)).thenReturn(Optional.empty());

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> tfcService.getTFCsByModule(12L, 7L));

    assertEquals(404, exception.getStatusCode().value());
    assertTrue(exception.getReason().contains("Module not found"));
  }

  @Test
  void getTfcById_returnsOwnedTfcWithEntriesNewestFirst() {
    TFC tfc = new TFC(module, owner, "Trees");
    ReflectionTestUtils.setField(tfc, "id", 81L);
    ReflectionTestUtils.setField(tfc, "updatedAt", java.time.LocalDateTime.of(2026, 6, 15, 15, 30));

    CFCEntry olderEntry = buildEntry(401L, "Trees");
    ReflectionTestUtils.setField(olderEntry, "createdAt", java.time.LocalDateTime.of(2026, 6, 14, 9, 0));

    CFCEntry newerEntry = buildEntry(402L, "Trees");
    ReflectionTestUtils.setField(newerEntry, "createdAt", java.time.LocalDateTime.of(2026, 6, 15, 9, 0));

    tfc.addEntry(olderEntry);
    tfc.addEntry(newerEntry);

    when(tfcRepository.findByIdAndOwnerId(81L, 7L)).thenReturn(Optional.of(tfc));

    TfcContentResponse response = tfcService.getTFCById(81L, 7L);

    assertEquals(81L, response.getId());
    assertEquals(12L, response.getModuleId());
    assertEquals("CS2040", response.getCourseCode());
    assertEquals("Trees", response.getTopic());
    assertEquals(2, response.getEntries().size());
    assertEquals(402L, response.getEntries().get(0).getEntryId());
    assertEquals("Flashcard question 402", response.getEntries().get(0).getFlashcardQuestion());
    assertEquals("Flashcard note 402", response.getEntries().get(0).getFlashcardNoteContent());
    assertEquals("Question 402", response.getEntries().get(0).getQuestionText());
    assertEquals("Note 402", response.getEntries().get(0).getRoughNote());
    assertEquals(401L, response.getEntries().get(1).getEntryId());
  }

  @Test
  void getTfcById_withUnownedTfc_throwsNotFound() {
    when(tfcRepository.findByIdAndOwnerId(81L, 7L)).thenReturn(Optional.empty());

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> tfcService.getTFCById(81L, 7L));

    assertEquals(404, exception.getStatusCode().value());
    assertTrue(exception.getReason().contains("TFC not found"));
  }

  private CFCEntry buildEntry(Long entryId, String topic) {
    CFC cfc = new CFC(module, SourceType.TUTORIAL, "Tutorial 5", "BST Revision", "Summary");
    CFCEntry entry = new CFCEntry(
        cfc,
        entryId,
        topic,
        "Question " + entryId,
        "Note " + entryId,
        new GeneratedCFCPage("Flashcard question " + entryId, "Flashcard note " + entryId));
    ReflectionTestUtils.setField(entry, "id", entryId);
    return entry;
  }
}
