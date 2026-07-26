package com.mindmesh.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import com.mindmesh.backend.dto.responses.sharing.MergeSharedTCResponseDto;
import com.mindmesh.backend.entity.CFC;
import com.mindmesh.backend.entity.CourseModule;
import com.mindmesh.backend.entity.SharedTC;
import com.mindmesh.backend.entity.SharedTCEntry;
import com.mindmesh.backend.entity.TC;
import com.mindmesh.backend.entity.User;
import com.mindmesh.backend.enums.CFCEntryOrigin;
import com.mindmesh.backend.enums.SharedTCStatus;
import com.mindmesh.backend.enums.SourceType;
import com.mindmesh.backend.repository.CFCRepository;
import com.mindmesh.backend.repository.SharedTCRepository;
import com.mindmesh.backend.repository.TCRepository;

@ExtendWith(MockitoExtension.class)
class SharedTCMergeServiceTest {

  @Mock private SharedTCRepository sharedTcRepository;
  @Mock private TCRepository tcRepository;
  @Mock private CFCRepository cfcRepository;
  @Mock private TCUpdateEventPublisher eventPublisher;
  @Mock private SharedTC sharedTc;
  @Mock private TC ownedTc;
  @Mock private CourseModule module;
  @Mock private User owner;
  @Mock private SharedTCEntry firstEntry;
  @Mock private SharedTCEntry secondEntry;

  private SharedTCMergeService mergeService;

  @BeforeEach
  void setUp() {
    mergeService = new SharedTCMergeService(
        sharedTcRepository,
        tcRepository,
        new TCEntryCopyService(cfcRepository),
        eventPublisher);
  }

  @Test
  void mergeCopiesEntriesPreservesSourceAndPublishesUpdate() {
    when(sharedTc.getId()).thenReturn(31L);
    when(sharedTc.getOwner()).thenReturn(owner);
    when(sharedTc.getModule()).thenReturn(module);
    when(sharedTc.getTopic()).thenReturn("Trees");
    when(sharedTc.getOriginalOwnerUsername()).thenReturn("alice");
    when(sharedTc.getStatus()).thenReturn(SharedTCStatus.ACTIVE, SharedTCStatus.MERGED);
    when(sharedTc.getEntries()).thenReturn(List.of(secondEntry, firstEntry));
    when(owner.getId()).thenReturn(7L);
    when(module.getId()).thenReturn(8L);
    when(ownedTc.getId()).thenReturn(17L);
    when(ownedTc.getOwner()).thenReturn(owner);
    when(ownedTc.getModule()).thenReturn(module);
    when(ownedTc.getTopic()).thenReturn("Trees");
    stubEntry(firstEntry, 401L, 0);
    stubEntry(secondEntry, 402L, 1);
    when(sharedTcRepository.findLockedByIdAndOwnerId(31L, 7L)).thenReturn(Optional.of(sharedTc));
    when(tcRepository.findMatchingOwnedTcForUpdate(7L, 8L, "Trees")).thenReturn(Optional.of(ownedTc));
    when(cfcRepository.saveAndFlush(any(CFC.class))).thenAnswer(invocation -> invocation.getArgument(0));

    MergeSharedTCResponseDto response = mergeService.merge(31L, 7L);

    ArgumentCaptor<CFC> cfcCaptor = ArgumentCaptor.forClass(CFC.class);
    verify(cfcRepository).saveAndFlush(cfcCaptor.capture());
    CFC imported = cfcCaptor.getValue();
    assertEquals(SourceType.SHARED_TC, imported.getSourceType());
    assertEquals(2, imported.getEntries().size());
    assertEquals(CFCEntryOrigin.MERGED_SHARED, imported.getEntries().get(0).getOrigin());
    assertEquals(SourceType.TUTORIAL, imported.getEntries().get(0).getSourceTypeAtShare());
    assertEquals("Tutorial 1", imported.getEntries().get(0).getSourceTitleAtShare());
    assertEquals("alice", imported.getEntries().get(0).getSourceOwnerUsername());
    verify(ownedTc).addEntry(imported.getEntries().get(0));
    verify(ownedTc).addEntry(imported.getEntries().get(1));
    verify(sharedTc).markMerged(any(TC.class), any(Instant.class));
    verify(eventPublisher).publishUpdated(ownedTc);
    assertEquals(17L, response.ownedTcId());
    assertEquals(2, response.mergedEntryCount());
    assertEquals(SharedTCStatus.MERGED, response.status());
  }

  @Test
  void mergeRejectsMissingOwnedTc() {
    when(sharedTcRepository.findLockedByIdAndOwnerId(31L, 7L)).thenReturn(Optional.of(sharedTc));
    when(sharedTc.getStatus()).thenReturn(SharedTCStatus.ACTIVE);
    when(sharedTc.getModule()).thenReturn(module);
    when(sharedTc.getTopic()).thenReturn("Trees");
    when(module.getId()).thenReturn(8L);
    when(tcRepository.findMatchingOwnedTcForUpdate(7L, 8L, "Trees")).thenReturn(Optional.empty());

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class, () -> mergeService.merge(31L, 7L));

    assertEquals(409, exception.getStatusCode().value());
    verify(cfcRepository, never()).saveAndFlush(any());
    verify(eventPublisher, never()).publishUpdated(any());
  }

  @Test
  void mergeByAnotherUserReturnsNotFound() {
    when(sharedTcRepository.findLockedByIdAndOwnerId(31L, 99L)).thenReturn(Optional.empty());

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class, () -> mergeService.merge(31L, 99L));

    assertEquals(404, exception.getStatusCode().value());
    verify(tcRepository, never()).findMatchingOwnedTcForUpdate(any(), any(), any());
  }

  @Test
  void mergeRejectsAlreadyMergedSnapshot() {
    when(sharedTcRepository.findLockedByIdAndOwnerId(31L, 7L)).thenReturn(Optional.of(sharedTc));
    when(sharedTc.getStatus()).thenReturn(SharedTCStatus.MERGED);

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class, () -> mergeService.merge(31L, 7L));

    assertEquals(409, exception.getStatusCode().value());
    verify(tcRepository, never()).findMatchingOwnedTcForUpdate(any(), any(), any());
    verify(eventPublisher, never()).publishUpdated(any());
  }

  private void stubEntry(SharedTCEntry entry, Long id, int order) {
    when(entry.getId()).thenReturn(id);
    when(entry.getDisplayOrder()).thenReturn(order);
    when(entry.getFlashcardQuestion()).thenReturn("Question " + (order + 1));
    when(entry.getFlashcardNoteContent()).thenReturn("Note " + (order + 1));
    when(entry.getQuestionText()).thenReturn("Original " + (order + 1));
    when(entry.getRoughNote()).thenReturn("Rough " + (order + 1));
    when(entry.getSourceType()).thenReturn(SourceType.TUTORIAL);
    when(entry.getSourceTitle()).thenReturn("Tutorial " + (order + 1));
    when(entry.getSourceEntryCreatedAt())
        .thenReturn(LocalDateTime.of(2026, 7, 1 + order, 10, 0));
  }
}
