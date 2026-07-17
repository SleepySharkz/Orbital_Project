package com.mindmesh.backend.service;

import java.time.Instant;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.mindmesh.backend.dto.ai.AIGeneratedInsightResponse;
import com.mindmesh.backend.dto.ai.AIInsightGenerationRequest;
import com.mindmesh.backend.service.ai.AIInsightGenerationService;

@Service
public class MindmapInsightGenerationWorker {

  private static final Logger LOGGER = LoggerFactory.getLogger(
      MindmapInsightGenerationWorker.class);

  private final TCInsightService tcInsightService;
  private final AIInsightGenerationService aiInsightGenerationService;

  public MindmapInsightGenerationWorker(
      TCInsightService tcInsightService,
      AIInsightGenerationService aiInsightGenerationService) {
    this.tcInsightService = tcInsightService;
    this.aiInsightGenerationService = aiInsightGenerationService;
  }

  @Async("mindmapInsightExecutor")
  public void generateInsight(Long insightId) {
    try {
      Optional<AIInsightGenerationRequest> request =
          tcInsightService.buildGenerationRequest(insightId);

      if (request.isEmpty()) {
        return;
      }

      AIGeneratedInsightResponse generated =
          aiInsightGenerationService.generateInsight(request.get());
      tcInsightService.completeGeneration(
          insightId,
          generated,
          request.get(),
          Instant.now());
    } catch (RuntimeException exception) {
      LOGGER.warn("Insight generation failed for insight {}.", insightId, exception);
      tcInsightService.markGenerationFailed(insightId);
    }
  }
}