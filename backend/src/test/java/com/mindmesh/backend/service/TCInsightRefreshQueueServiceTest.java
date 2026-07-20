package com.mindmesh.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mindmesh.backend.entity.TC;
import com.mindmesh.backend.entity.TCInsight;
import com.mindmesh.backend.entity.TCInsightRefreshJob;
import com.mindmesh.backend.enums.TCInsightRefreshJobStatus;
import com.mindmesh.backend.enums.TCInsightStatus;
import com.mindmesh.backend.event.TCUpdatedEvent;
import com.mindmesh.backend.repository.TCInsightRefreshJobRepository;
import com.mindmesh.backend.repository.TCInsightRepository;

@ExtendWith(MockitoExtension.class)
class TCInsightRefreshQueueServiceTest {

  @Mock private TCInsightRepository insightRepository;
  @Mock private TCInsightRefreshJobRepository jobRepository;
  @Mock private TCInsight firstInsight;
  @Mock private TCInsight secondInsight;
  @Mock private TC tcA;
  @Mock private TC tcB;
  @Mock private TC tcC;

  private TCInsightRefreshQueueService service;

  @BeforeEach
  void setUp() {
    service = new TCInsightRefreshQueueService(insightRepository, jobRepository);
    when(tcA.getId()).thenReturn(10L);
    when(tcB.getId()).thenReturn(11L);
  }

  @Test
  void updateQueuesEveryAffectedInsight() {
    when(tcC.getId()).thenReturn(12L);
    stubInsight(firstInsight, 100L, tcA, tcB, "old-a", "old-b");
    stubInsight(secondInsight, 101L, tcA, tcC, "old-a", "old-c");
    when(insightRepository.findAllAffectedByTcIds(1L, 2L, java.util.Set.of(10L)))
        .thenReturn(List.of(firstInsight, secondInsight));
    when(jobRepository.findAllByInsightIdIn(List.of(100L, 101L))).thenReturn(List.of());

    service.queueForUpdates(List.of(event("new-a")));

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Iterable<TCInsightRefreshJob>> captor = ArgumentCaptor.forClass(Iterable.class);
    verify(jobRepository).saveAll(captor.capture());
    List<TCInsightRefreshJob> jobs = java.util.stream.StreamSupport
        .stream(captor.getValue().spliterator(), false).toList();
    assertEquals(2, jobs.size());
    assertTrue(jobs.stream().allMatch(job -> "new-a".equals(job.getTargetTcAContentHash())));
    verify(firstInsight).markRefreshing(any(Instant.class));
    verify(secondInsight).markRefreshing(any(Instant.class));
  }

  @Test
  void updateDuringRunningJobRequestsOneRerunWithLatestHash() {
    stubInsight(firstInsight, 100L, tcA, tcB, "old-a", "old-b");
    when(firstInsight.getStatus()).thenReturn(TCInsightStatus.REFRESHING);
    TCInsightRefreshJob job = new TCInsightRefreshJob(firstInsight, "old-a", "old-b");
    job.markRunning(Instant.now());
    when(insightRepository.findAllAffectedByTcIds(1L, 2L, java.util.Set.of(10L)))
        .thenReturn(List.of(firstInsight));
    when(jobRepository.findAllByInsightIdIn(List.of(100L))).thenReturn(List.of(job));

    service.queueForUpdates(List.of(event("latest-a")));

    assertEquals(TCInsightRefreshJobStatus.RUNNING, job.getStatus());
    assertEquals("latest-a", job.getTargetTcAContentHash());
    assertTrue(job.getRerunRequested());
    verify(jobRepository, never()).saveAll(any());
  }

  private void stubInsight(
      TCInsight insight,
      Long id,
      TC first,
      TC second,
      String hashA,
      String hashB) {
    when(insight.getId()).thenReturn(id);
    when(insight.getTcA()).thenReturn(first);
    when(insight.getTcB()).thenReturn(second);
    when(insight.getTcAContentHashAtGeneration()).thenReturn(hashA);
    when(insight.getTcBContentHashAtGeneration()).thenReturn(hashB);
    when(insight.getStatus()).thenReturn(TCInsightStatus.READY);
  }

  private TCUpdatedEvent event(String hash) {
    return new TCUpdatedEvent(1L, 2L, 10L, "Trees", hash, Instant.now());
  }
}
