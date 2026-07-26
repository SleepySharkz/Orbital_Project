package com.mindmesh.backend.service;

import java.time.Instant;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mindmesh.backend.dto.ai.AIGeneratedInsightResponse;
import com.mindmesh.backend.dto.ai.AIInsightGenerationRequest;
import com.mindmesh.backend.entity.TCInsight;
import com.mindmesh.backend.entity.TCInsightRefreshJob;
import com.mindmesh.backend.enums.TCInsightRefreshJobStatus;
import com.mindmesh.backend.enums.TCInsightStatus;
import com.mindmesh.backend.repository.TCInsightRefreshJobRepository;

@Service
public class TCInsightRefreshJobService {

  private static final int MAX_ERROR_LENGTH = 1000;

  private final TCInsightRefreshJobRepository refreshJobRepository;
  private final TCInsightService tcInsightService;

  public TCInsightRefreshJobService(
      TCInsightRefreshJobRepository refreshJobRepository,
      TCInsightService tcInsightService) {
    this.refreshJobRepository = refreshJobRepository;
    this.tcInsightService = tcInsightService;
  }

  @Transactional
  public Optional<RefreshClaim> claim(Long jobId) {
    TCInsightRefreshJob job = refreshJobRepository.findLockedById(jobId).orElse(null);
    if (job == null || job.getStatus() != TCInsightRefreshJobStatus.PENDING) {
      return Optional.empty();
    }

    TCInsight insight = job.getInsight();
    if (insight.getStatus() == TCInsightStatus.GENERATING) {
      return Optional.empty();
    }

    Instant startedAt = Instant.now();
    job.markRunning(startedAt);
    insight.markRefreshing(startedAt);
    return Optional.of(new RefreshClaim(
        job.getId(),
        insight.getId(),
        insight.getTcAContentHashAtGeneration(),
        insight.getTcBContentHashAtGeneration()));
  }

  @Transactional
  public void completeWithoutChanges(
      Long jobId,
      AIInsightGenerationRequest request,
      Instant completedAt) {
    TCInsightRefreshJob job = runningJob(jobId);
    if (job == null) {
      return;
    }
    if (job.requiresRerun(request.tcAContentHash(), request.tcBContentHash())) {
      job.markPendingForRerun();
      return;
    }
    job.getInsight().completeUnchangedRefresh(completedAt);
    job.markSucceeded(completedAt);
  }

  @Transactional
  public void completeSuccess(
      Long jobId,
      AIGeneratedInsightResponse generated,
      AIInsightGenerationRequest request,
      Instant completedAt) {
    TCInsightRefreshJob job = runningJob(jobId);
    if (job == null) {
      return;
    }
    if (job.requiresRerun(request.tcAContentHash(), request.tcBContentHash())) {
      job.markPendingForRerun();
      return;
    }
    tcInsightService.applyRefreshResult(job.getInsight(), generated, request, completedAt);
    job.markSucceeded(completedAt);
  }

  @Transactional
  public void completeFailure(Long jobId, String errorMessage, Instant completedAt) {
    TCInsightRefreshJob job = runningJob(jobId);
    if (job == null) {
      return;
    }
    if (Boolean.TRUE.equals(job.getRerunRequested())) {
      job.markPendingForRerun();
      return;
    }
    job.getInsight().markRefreshFailed(completedAt);
    job.markFailed(truncate(errorMessage), completedAt);
  }

  private TCInsightRefreshJob runningJob(Long jobId) {
    return refreshJobRepository.findLockedById(jobId)
        .filter(job -> job.getStatus() == TCInsightRefreshJobStatus.RUNNING)
        .orElse(null);
  }

  private String truncate(String errorMessage) {
    String value = errorMessage == null || errorMessage.isBlank()
        ? "Unknown refresh failure"
        : errorMessage.trim();
    return value.length() <= MAX_ERROR_LENGTH ? value : value.substring(0, MAX_ERROR_LENGTH);
  }

  public record RefreshClaim(
      Long jobId,
      Long insightId,
      String storedTcAContentHash,
      String storedTcBContentHash) {
  }
}
