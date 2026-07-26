package com.mindmesh.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mindmesh.backend.dto.responses.mindmap.MindmapInsightDetailDto;
import com.mindmesh.backend.enums.TCInsightStatus;
import com.mindmesh.backend.service.TCInsightService.InsightReservation;

@ExtendWith(MockitoExtension.class)
class MindmapInsightDiscoveryServiceTest {

  @Mock
  private TCInsightService tcInsightService;

  @Mock
  private MindmapInsightGenerationWorker generationWorker;

  private MindmapInsightDiscoveryService discoveryService;

  @BeforeEach
  void setUp() {
    discoveryService = new MindmapInsightDiscoveryService(
        tcInsightService,
        generationWorker);
  }

  @Test
  void discover_newReservation_enqueuesGeneration() {
    when(tcInsightService.reserveDiscoveryInsight(7L, 12L, List.of(101L, 102L)))
        .thenReturn(new InsightReservation(44L, true));
    when(tcInsightService.getInsightDetail(44L, 7L))
        .thenReturn(detail());

    MindmapInsightDetailDto result = discoveryService.discover(
        7L,
        12L,
        List.of(101L, 102L));

    assertEquals(TCInsightStatus.GENERATING, result.getStatus());
    verify(generationWorker).generateInsight(44L);
  }

  @Test
  void discover_existingReservation_doesNotEnqueueAgain() {
    when(tcInsightService.reserveDiscoveryInsight(7L, 12L, List.of(101L, 102L)))
        .thenReturn(new InsightReservation(44L, false));
    when(tcInsightService.getInsightDetail(44L, 7L))
        .thenReturn(detail());

    discoveryService.discover(7L, 12L, List.of(101L, 102L));

    verify(generationWorker, never()).generateInsight(44L);
  }

  private MindmapInsightDetailDto detail() {
    return new MindmapInsightDetailDto(
        44L,
        12L,
        101L,
        102L,
        "Trees",
        "Graphs",
        TCInsightStatus.GENERATING,
        null,
        null,
        List.of(),
        null,
        null,
        null,
        null);
  }
}
