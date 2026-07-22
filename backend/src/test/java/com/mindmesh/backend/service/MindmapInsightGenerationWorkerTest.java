package com.mindmesh.backend.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mindmesh.backend.dto.ai.AIGeneratedInsightPoint;
import com.mindmesh.backend.dto.ai.AIGeneratedInsightResponse;
import com.mindmesh.backend.dto.ai.AIInsightGenerationRequest;
import com.mindmesh.backend.service.ai.AIInsightGenerationService;

@ExtendWith(MockitoExtension.class)
class MindmapInsightGenerationWorkerTest {

  @Mock
  private TCInsightService tcInsightService;

  @Mock
  private AIInsightGenerationService aiInsightGenerationService;

  private MindmapInsightGenerationWorker worker;
  private AIInsightGenerationRequest request;

  @BeforeEach
  void setUp() {
    worker = new MindmapInsightGenerationWorker(
        tcInsightService,
        aiInsightGenerationService);
    request = new AIInsightGenerationRequest(
        "CS2040S",
        "Year1Sem2",
        "Trees",
        "Graphs",
        List.of(),
        List.of(),
        "hash-a",
        "hash-b");
  }

  @Test
  void generateInsight_successCompletesTheReservation() {
    AIGeneratedInsightResponse generated = new AIGeneratedInsightResponse(
        true,
        "Trees as graphs",
        "Trees are connected acyclic graphs.",
        List.of(new AIGeneratedInsightPoint(
            "Acyclicity",
            "The absence of cycles changes traversal assumptions.",
            "Tree definition",
            "Graph cycle notes")),
        null);
    when(tcInsightService.buildGenerationRequest(44L))
        .thenReturn(Optional.of(request));
    when(aiInsightGenerationService.generateInsight(request)).thenReturn(generated);

    worker.generateInsight(44L);

    verify(tcInsightService).completeGeneration(
        eq(44L),
        eq(generated),
        eq(request),
        any(Instant.class));
  }

  @Test
  void generateInsight_aiFailureMarksTheReservationFailed() {
    when(tcInsightService.buildGenerationRequest(44L))
        .thenReturn(Optional.of(request));
    when(aiInsightGenerationService.generateInsight(request))
        .thenThrow(new IllegalStateException("provider failure"));

    worker.generateInsight(44L);

    verify(tcInsightService).markGenerationFailed(44L);
  }
}
