package com.mindmesh.backend.service;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.mindmesh.backend.dto.ai.AIGeneratedInsightResponse;
import com.mindmesh.backend.dto.ai.AIInsightGenerationRequest;
import com.mindmesh.backend.service.TCInsightRefreshJobService.RefreshClaim;
import com.mindmesh.backend.service.ai.AIInsightGenerationService;

@Service
public class MindmapInsightRefreshWorker {

  private static final Logger LOGGER = LoggerFactory.getLogger(MindmapInsightRefreshWorker.class);

  private final TCInsightRefreshJobService refreshJobService;
  private final TCInsightService tcInsightService;
  private final AIInsightGenerationService aiInsightGenerationService;

  public MindmapInsightRefreshWorker(
      TCInsightRefreshJobService refreshJobService,
      TCInsightService tcInsightService,
      AIInsightGenerationService aiInsightGenerationService) {
    this.refreshJobService = refreshJobService;
    this.tcInsightService = tcInsightService;
    this.aiInsightGenerationService = aiInsightGenerationService;
  }

  @Async("mindmapRefreshExecutor")
  public void refresh(Long jobId) {
    Optional<RefreshClaim> claim = refreshJobService.claim(jobId);
    if (claim.isEmpty()) {
      return;
    }

    try {
      AIInsightGenerationRequest request = tcInsightService
          .buildRefreshRequest(claim.get().insightId())
          .orElseThrow(() -> new IllegalStateException("Refresh insight is not available."));

      if (hashesUnchanged(claim.get(), request)) {
        refreshJobService.completeWithoutChanges(jobId, request, Instant.now());
        return;
      }

      AIGeneratedInsightResponse generated = aiInsightGenerationService.generateInsight(request);
      refreshJobService.completeSuccess(jobId, generated, request, Instant.now());
    } catch (RuntimeException exception) {
      LOGGER.warn("Insight refresh failed for job {}.", jobId, exception);
      refreshJobService.completeFailure(jobId, technicalMessage(exception), Instant.now());
    }
  }

  private boolean hashesUnchanged(RefreshClaim claim, AIInsightGenerationRequest request) {
    return Objects.equals(claim.storedTcAContentHash(), request.tcAContentHash())
        && Objects.equals(claim.storedTcBContentHash(), request.tcBContentHash());
  }

  private String technicalMessage(RuntimeException exception) {
    String detail = exception.getMessage();
    return exception.getClass().getSimpleName()
        + (detail == null || detail.isBlank() ? "" : ": " + detail);
  }
}
