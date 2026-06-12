package com.mindmesh.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
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

import com.mindmesh.backend.entity.CFC;
import com.mindmesh.backend.entity.CFCEntry;
import com.mindmesh.backend.entity.CourseModule;
import com.mindmesh.backend.entity.GeneratedCFCPage;
import com.mindmesh.backend.entity.ModuleTopic;
import com.mindmesh.backend.entity.TFC;
import com.mindmesh.backend.entity.User;
import com.mindmesh.backend.enums.SourceType;
import com.mindmesh.backend.repository.CFCEntryRepository;
import com.mindmesh.backend.repository.TFCRepository;

@ExtendWith(MockitoExtension.class)
class TFCServiceTest {

  @Mock
  private TFCRepository tfcRepository;

  @Mock
  private CFCEntryRepository cfcEntryRepository;

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
