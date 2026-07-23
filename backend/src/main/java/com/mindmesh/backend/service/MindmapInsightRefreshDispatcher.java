package com.mindmesh.backend.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.mindmesh.backend.enums.TCInsightRefreshJobStatus;
import com.mindmesh.backend.repository.TCInsightRefreshJobRepository;

@Component
public class MindmapInsightRefreshDispatcher {

  private static final Logger LOGGER = LoggerFactory.getLogger(
      MindmapInsightRefreshDispatcher.class);

  private final TCInsightRefreshJobRepository refreshJobRepository;
  private final MindmapInsightRefreshWorker refreshWorker;
  private final int batchSize;

  public MindmapInsightRefreshDispatcher(
      TCInsightRefreshJobRepository refreshJobRepository,
      MindmapInsightRefreshWorker refreshWorker,
      @Value("${mindmesh.mindmap.refresh.dispatch-batch-size:20}") int batchSize) {
    this.refreshJobRepository = refreshJobRepository;
    this.refreshWorker = refreshWorker;
    this.batchSize = batchSize;
  }

  @Scheduled(fixedDelayString = "${mindmesh.mindmap.refresh.dispatch-delay-ms:1000}")
  public void dispatchPendingJobs() {
    List<Long> pendingJobIds = refreshJobRepository.findIdsByStatus(
        TCInsightRefreshJobStatus.PENDING,
        PageRequest.of(0, batchSize));

    for (Long jobId : pendingJobIds) {
      try {
        refreshWorker.refresh(jobId);
      } catch (TaskRejectedException exception) {
        LOGGER.debug("Refresh executor is full; pending jobs will be retried.");
        break;
      }
    }
  }
}
