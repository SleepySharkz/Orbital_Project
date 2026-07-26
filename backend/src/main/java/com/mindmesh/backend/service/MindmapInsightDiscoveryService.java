package com.mindmesh.backend.service;

import java.util.List;

import org.springframework.core.task.TaskRejectedException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.mindmesh.backend.dto.responses.mindmap.MindmapInsightDetailDto;
import com.mindmesh.backend.service.TCInsightService.InsightReservation;

@Service
public class MindmapInsightDiscoveryService {

  private final TCInsightService tcInsightService;
  private final MindmapInsightGenerationWorker generationWorker;

  public MindmapInsightDiscoveryService(
      TCInsightService tcInsightService,
      MindmapInsightGenerationWorker generationWorker) {
    this.tcInsightService = tcInsightService;
    this.generationWorker = generationWorker;
  }

  public MindmapInsightDetailDto discover(
      Long userId,
      Long moduleId,
      List<Long> selectedTcIds) {
    InsightReservation reservation;

    try {
      reservation = tcInsightService.reserveDiscoveryInsight(
          userId,
          moduleId,
          selectedTcIds);
    } catch (DataIntegrityViolationException exception) {
      reservation = tcInsightService.findExistingReservationAfterConflict(
          userId,
          moduleId,
          selectedTcIds);
    }

    if (reservation.generationRequired()) {
      try {
        generationWorker.generateInsight(reservation.insightId());
      } catch (TaskRejectedException exception) {
        tcInsightService.markGenerationFailed(reservation.insightId());
        throw new ResponseStatusException(
            HttpStatus.SERVICE_UNAVAILABLE,
            "Insight generation is busy. Please try again.",
            exception);
      }
    }

    return tcInsightService.getInsightDetail(reservation.insightId(), userId);
  }

  public MindmapInsightDetailDto getDetail(Long insightId, Long userId) {
    return tcInsightService.getInsightDetail(insightId, userId);
  }
}