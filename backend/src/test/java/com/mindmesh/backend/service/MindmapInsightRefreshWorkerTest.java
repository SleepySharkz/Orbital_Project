package com.mindmesh.backend.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mindmesh.backend.dto.ai.AIInsightGenerationRequest;
import com.mindmesh.backend.service.TCInsightRefreshJobService.RefreshClaim;
import com.mindmesh.backend.service.ai.AIInsightGenerationService;

@ExtendWith(MockitoExtension.class)
class MindmapInsightRefreshWorkerTest {

  @Mock private TCInsightRefreshJobService jobService;
  @Mock private TCInsightService insightService;
  @Mock private AIInsightGenerationService aiService;

  @Test
  void unchangedHashesSkipAi() {
    AIInsightGenerationRequest request = new AIInsightGenerationRequest(
        "CS2040", "Y1S2", "Trees", "Graphs", List.of(), List.of(), "a", "b");
    when(jobService.claim(4L)).thenReturn(Optional.of(new RefreshClaim(4L, 9L, "a", "b")));
    when(insightService.buildRefreshRequest(9L)).thenReturn(Optional.of(request));

    new MindmapInsightRefreshWorker(jobService, insightService, aiService).refresh(4L);

    verify(aiService, never()).generateInsight(any());
    verify(jobService).completeWithoutChanges(eq(4L), eq(request), any());
  }
}
