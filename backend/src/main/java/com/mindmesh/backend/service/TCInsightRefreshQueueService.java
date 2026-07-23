package com.mindmesh.backend.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mindmesh.backend.entity.TCInsight;
import com.mindmesh.backend.entity.TCInsightRefreshJob;
import com.mindmesh.backend.enums.TCInsightStatus;
import com.mindmesh.backend.event.TCUpdatedEvent;
import com.mindmesh.backend.repository.TCInsightRefreshJobRepository;
import com.mindmesh.backend.repository.TCInsightRepository;

@Service
public class TCInsightRefreshQueueService {

  private final TCInsightRepository tcInsightRepository;
  private final TCInsightRefreshJobRepository refreshJobRepository;

  public TCInsightRefreshQueueService(
      TCInsightRepository tcInsightRepository,
      TCInsightRefreshJobRepository refreshJobRepository) {
    this.tcInsightRepository = tcInsightRepository;
    this.refreshJobRepository = refreshJobRepository;
  }

  @Transactional
  public void queueForUpdates(List<TCUpdatedEvent> events) {
    if (events == null || events.isEmpty()) {
      return;
    }

    Map<OwnerModuleKey, List<TCUpdatedEvent>> groupedEvents = events.stream()
        .collect(Collectors.groupingBy(event -> new OwnerModuleKey(
            event.userId(), event.moduleId())));

    for (Map.Entry<OwnerModuleKey, List<TCUpdatedEvent>> group : groupedEvents.entrySet()) {
      queueGroup(group.getKey(), group.getValue());
    }
  }

  private void queueGroup(OwnerModuleKey key, List<TCUpdatedEvent> events) {
    Map<Long, TCUpdatedEvent> latestByTcId = events.stream()
        .collect(Collectors.toMap(
            TCUpdatedEvent::tcId,
            Function.identity(),
            this::laterEvent));

    List<TCInsight> affectedInsights = tcInsightRepository.findAllAffectedByTcIds(
        key.userId(), key.moduleId(), latestByTcId.keySet());
    if (affectedInsights.isEmpty()) {
      return;
    }

    List<Long> insightIds = affectedInsights.stream().map(TCInsight::getId).toList();
    Map<Long, TCInsightRefreshJob> jobsByInsightId = refreshJobRepository
        .findAllByInsightIdIn(insightIds).stream()
        .collect(Collectors.toMap(job -> job.getInsight().getId(), Function.identity()));

    List<TCInsightRefreshJob> newJobs = new ArrayList<>();
    Instant queuedAt = Instant.now();
    for (TCInsight insight : affectedInsights) {
      TCInsightRefreshJob existingJob = jobsByInsightId.get(insight.getId());
      String targetHashA = targetHash(
          insight.getTcA().getId(),
          latestByTcId,
          existingJob == null ? null : existingJob.getTargetTcAContentHash(),
          insight.getTcAContentHashAtGeneration());
      String targetHashB = targetHash(
          insight.getTcB().getId(),
          latestByTcId,
          existingJob == null ? null : existingJob.getTargetTcBContentHash(),
          insight.getTcBContentHashAtGeneration());

      if (existingJob == null) {
        newJobs.add(new TCInsightRefreshJob(insight, targetHashA, targetHashB));
      } else {
        existingJob.requestRefresh(targetHashA, targetHashB);
      }

      if (insight.getStatus() != TCInsightStatus.GENERATING
          && insight.getStatus() != TCInsightStatus.REFRESHING) {
        insight.markRefreshing(queuedAt);
      }
    }

    if (!newJobs.isEmpty()) {
      refreshJobRepository.saveAll(newJobs);
      refreshJobRepository.flush();
    }
  }

  private String targetHash(
      Long tcId,
      Map<Long, TCUpdatedEvent> latestByTcId,
      String existingJobHash,
      String generatedHash) {
    TCUpdatedEvent updatedEvent = latestByTcId.get(tcId);
    if (updatedEvent != null) {
      return updatedEvent.contentHash();
    }
    return existingJobHash != null ? existingJobHash : generatedHash;
  }

  private TCUpdatedEvent laterEvent(TCUpdatedEvent first, TCUpdatedEvent second) {
    return second.updatedAt().isAfter(first.updatedAt()) ? second : first;
  }

  private record OwnerModuleKey(Long userId, Long moduleId) {}
}
