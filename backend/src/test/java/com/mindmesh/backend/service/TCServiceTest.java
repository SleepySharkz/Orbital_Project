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

import com.mindmesh.backend.dto.responses.tc.TcContentResponse;
import com.mindmesh.backend.dto.responses.tc.TcSummaryResponse;
import com.mindmesh.backend.entity.CFC;
import com.mindmesh.backend.entity.CFCEntry;
import com.mindmesh.backend.entity.CourseModule;
import com.mindmesh.backend.entity.GeneratedCFCPage;
import com.mindmesh.backend.entity.ModuleTopic;
import com.mindmesh.backend.entity.TC;
import com.mindmesh.backend.entity.User;
import com.mindmesh.backend.enums.SourceType;
import com.mindmesh.backend.repository.CFCEntryRepository;
import com.mindmesh.backend.repository.CourseModuleRepository;
import com.mindmesh.backend.repository.TCRepository;

@ExtendWith(MockitoExtension.class)
class TCServiceTest {

  @Mock
  private TCRepository tcRepository;

  @Mock
  private CFCEntryRepository cfcEntryRepository;

  @Mock
  private CourseModuleRepository courseModuleRepository;

  @InjectMocks
  private TCService tcService;

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
  void syncTcForTopic_createsNewTcAndAttachesMatchingEntries() {
    CFCEntry treeEntryOne = buildEntry(101L, "Trees");
    CFCEntry treeEntryTwo = buildEntry(102L, "Trees");

    when(tcRepository.findByOwnerIdAndModuleIdAndTopic(7L, 12L, "Trees")).thenReturn(Optional.empty());
    when(cfcEntryRepository.findAllByCfcModuleUserIdAndCfcModuleIdAndTopic(7L, 12L, "Trees"))
        .thenReturn(List.of(treeEntryOne, treeEntryTwo));
    when(tcRepository.save(any(TC.class))).thenAnswer(invocation -> invocation.getArgument(0));

    tcService.syncTCForTopic(module, owner, "Trees");

    ArgumentCaptor<TC> tcCaptor = ArgumentCaptor.forClass(TC.class);
    verify(tcRepository).save(tcCaptor.capture());

    TC savedTc = tcCaptor.getValue();
    assertSame(module, savedTc.getModule());
    assertSame(owner, savedTc.getOwner());
    assertEquals("Trees", savedTc.getTopic());
    assertEquals(2, savedTc.getEntries().size());
    assertTrue(savedTc.getEntries().contains(treeEntryOne));
    assertTrue(savedTc.getEntries().contains(treeEntryTwo));
    assertSame(savedTc, treeEntryOne.getTc());
    assertSame(savedTc, treeEntryTwo.getTc());
  }

  @Test
  void syncTcForTopic_replacesMembershipByRemovingStaleAndAddingMissingEntries() {
    TC existingTc = new TC(module, owner, "Trees");
    ReflectionTestUtils.setField(existingTc, "id", 55L);

    CFCEntry keepEntry = buildEntry(201L, "Trees");
    CFCEntry staleEntry = buildEntry(202L, "Trees");
    CFCEntry newEntry = buildEntry(203L, "Trees");

    existingTc.addEntry(keepEntry);
    existingTc.addEntry(staleEntry);

    when(tcRepository.findByOwnerIdAndModuleIdAndTopic(7L, 12L, "Trees")).thenReturn(Optional.of(existingTc));
    when(cfcEntryRepository.findAllByCfcModuleUserIdAndCfcModuleIdAndTopic(7L, 12L, "Trees"))
        .thenReturn(List.of(keepEntry, newEntry));
    when(tcRepository.save(any(TC.class))).thenAnswer(invocation -> invocation.getArgument(0));

    tcService.syncTCForTopic(module, owner, "Trees");

    assertEquals(2, existingTc.getEntries().size());
    assertTrue(existingTc.getEntries().contains(keepEntry));
    assertTrue(existingTc.getEntries().contains(newEntry));
    assertTrue(!existingTc.getEntries().contains(staleEntry));
    assertSame(existingTc, keepEntry.getTc());
    assertSame(existingTc, newEntry.getTc());
    assertNull(staleEntry.getTc());
    verify(tcRepository).save(existingTc);
  }

  @Test
  void syncTcForTopic_deletesExistingTcWhenNoMatchingEntriesRemain() {
    TC existingTc = new TC(module, owner, "Trees");
    ReflectionTestUtils.setField(existingTc, "id", 88L);

    when(tcRepository.findByOwnerIdAndModuleIdAndTopic(7L, 12L, "Trees")).thenReturn(Optional.of(existingTc));
    when(cfcEntryRepository.findAllByCfcModuleUserIdAndCfcModuleIdAndTopic(7L, 12L, "Trees"))
        .thenReturn(List.of());

    tcService.syncTCForTopic(module, owner, "Trees");

    verify(tcRepository).delete(existingTc);
    verify(tcRepository, never()).save(any(TC.class));
  }

  @Test
  void syncTcForTopic_doesNothingWhenNoExistingTcAndNoMatchingEntries() {
    when(tcRepository.findByOwnerIdAndModuleIdAndTopic(7L, 12L, "Trees")).thenReturn(Optional.empty());
    when(cfcEntryRepository.findAllByCfcModuleUserIdAndCfcModuleIdAndTopic(7L, 12L, "Trees"))
        .thenReturn(List.of());

    tcService.syncTCForTopic(module, owner, "Trees");

    verify(tcRepository, never()).delete(any(TC.class));
    verify(tcRepository, never()).save(any(TC.class));
  }

  @Test
  void getTcsByModule_returnsOwnedTcsSortedByUpdatedAtDesc() {
    TC newerTc = new TC(module, owner, "Graphs");
    ReflectionTestUtils.setField(newerTc, "id", 71L);
    ReflectionTestUtils.setField(newerTc, "updatedAt", java.time.LocalDateTime.of(2026, 6, 15, 12, 0));

    TC olderTc = new TC(module, owner, "Trees");
    ReflectionTestUtils.setField(olderTc, "id", 70L);
    ReflectionTestUtils.setField(olderTc, "updatedAt", java.time.LocalDateTime.of(2026, 6, 14, 12, 0));

    olderTc.addEntry(buildEntry(301L, "Trees"));
    olderTc.addEntry(buildEntry(302L, "Trees"));
    newerTc.addEntry(buildEntry(303L, "Graphs"));

    when(courseModuleRepository.findByIdAndUserId(12L, 7L)).thenReturn(Optional.of(module));
    when(tcRepository.findAllByOwnerIdAndModuleIdOrderByUpdatedAtDesc(7L, 12L))
        .thenReturn(List.of(newerTc, olderTc));

    List<TcSummaryResponse> responses = tcService.getTCsByModule(12L, 7L);

    assertEquals(2, responses.size());
    assertEquals(71L, responses.get(0).getId());
    assertEquals("Graphs", responses.get(0).getTopic());
    assertEquals("Tauzih", responses.get(0).getOwnerUsername());
    assertEquals(1, responses.get(0).getEntryCount());
    assertEquals(false, responses.get(0).getIsStale());
    assertEquals(70L, responses.get(1).getId());
    assertEquals("Trees", responses.get(1).getTopic());
    assertEquals(2, responses.get(1).getEntryCount());
    assertEquals(false, responses.get(1).getIsStale());
  }

  @Test
  void getTcsByModule_marksTcStaleWhenTopicNoLongerExistsOnModule() {
    TC staleTc = new TC(module, owner, "Trees");
    ReflectionTestUtils.setField(staleTc, "id", 90L);
    ReflectionTestUtils.setField(staleTc, "updatedAt", java.time.LocalDateTime.of(2026, 6, 18, 10, 0));

    module.removeTopic(module.getTopics().get(0));

    when(courseModuleRepository.findByIdAndUserId(12L, 7L)).thenReturn(Optional.of(module));
    when(tcRepository.findAllByOwnerIdAndModuleIdOrderByUpdatedAtDesc(7L, 12L))
        .thenReturn(List.of(staleTc));

    List<TcSummaryResponse> responses = tcService.getTCsByModule(12L, 7L);

    assertEquals(1, responses.size());
    assertEquals(true, responses.get(0).getIsStale());
  }

  @Test
  void getTcsByModule_withUnownedModule_throwsNotFound() {
    when(courseModuleRepository.findByIdAndUserId(12L, 7L)).thenReturn(Optional.empty());

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> tcService.getTCsByModule(12L, 7L));

    assertEquals(404, exception.getStatusCode().value());
    assertTrue(exception.getReason().contains("Module not found"));
  }

  @Test
  void getTcById_returnsOwnedTcWithEntriesNewestFirst() {
    TC tc = new TC(module, owner, "Trees");
    ReflectionTestUtils.setField(tc, "id", 81L);
    ReflectionTestUtils.setField(tc, "updatedAt", java.time.LocalDateTime.of(2026, 6, 15, 15, 30));

    CFCEntry olderEntry = buildEntry(401L, "Trees");
    ReflectionTestUtils.setField(olderEntry, "createdAt", java.time.LocalDateTime.of(2026, 6, 14, 9, 0));

    CFCEntry newerEntry = buildEntry(402L, "Trees");
    ReflectionTestUtils.setField(newerEntry, "createdAt", java.time.LocalDateTime.of(2026, 6, 15, 9, 0));

    tc.addEntry(olderEntry);
    tc.addEntry(newerEntry);

    when(tcRepository.findByIdAndOwnerId(81L, 7L)).thenReturn(Optional.of(tc));

    TcContentResponse response = tcService.getTCById(81L, 7L);

    assertEquals(81L, response.getId());
    assertEquals(12L, response.getModuleId());
    assertEquals("CS2040", response.getCourseCode());
    assertEquals("Trees", response.getTopic());
    assertEquals(false, response.getIsStale());
    assertEquals(2, response.getEntries().size());
    assertEquals(402L, response.getEntries().get(0).getEntryId());
    assertEquals("Flashcard question 402", response.getEntries().get(0).getFlashcardQuestion());
    assertEquals("Flashcard note 402", response.getEntries().get(0).getFlashcardNoteContent());
    assertEquals("Question 402", response.getEntries().get(0).getQuestionText());
    assertEquals("Note 402", response.getEntries().get(0).getRoughNote());
    assertEquals(401L, response.getEntries().get(1).getEntryId());
  }

  @Test
  void getTcById_returnsStaleFlagWhenTopicNoLongerExistsOnModule() {
    TC tc = new TC(module, owner, "Trees");
    ReflectionTestUtils.setField(tc, "id", 82L);
    ReflectionTestUtils.setField(tc, "updatedAt", java.time.LocalDateTime.of(2026, 6, 18, 12, 0));
    module.removeTopic(module.getTopics().get(0));

    when(tcRepository.findByIdAndOwnerId(82L, 7L)).thenReturn(Optional.of(tc));

    TcContentResponse response = tcService.getTCById(82L, 7L);

    assertEquals(true, response.getIsStale());
  }

  @Test
  void getTcById_withUnownedTc_throwsNotFound() {
    when(tcRepository.findByIdAndOwnerId(81L, 7L)).thenReturn(Optional.empty());

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> tcService.getTCById(81L, 7L));

    assertEquals(404, exception.getStatusCode().value());
    assertTrue(exception.getReason().contains("TC not found"));
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
