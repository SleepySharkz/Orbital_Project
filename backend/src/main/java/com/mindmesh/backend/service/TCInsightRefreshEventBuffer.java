package com.mindmesh.backend.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import com.mindmesh.backend.event.TCUpdatedEvent;

@Component
public class TCInsightRefreshEventBuffer {

  private static final Logger LOGGER = LoggerFactory.getLogger(TCInsightRefreshEventBuffer.class);

  private final Object monitor = new Object();
  private final Map<EventKey, TCUpdatedEvent> pendingEvents = new HashMap<>();
  private final TaskScheduler taskScheduler;
  private final TCInsightRefreshQueueService refreshQueueService;
  private final long debounceMs;
  private ScheduledFuture<?> scheduledFlush;

  public TCInsightRefreshEventBuffer(
      @Qualifier("mindmapRefreshTaskScheduler") TaskScheduler taskScheduler,
      TCInsightRefreshQueueService refreshQueueService,
      @Value("${mindmesh.mindmap.refresh.debounce-ms:3000}") long debounceMs) {
    this.taskScheduler = taskScheduler;
    this.refreshQueueService = refreshQueueService;
    this.debounceMs = debounceMs;
  }

  public void enqueue(TCUpdatedEvent event) {
    synchronized (monitor) {
      mergeLatest(event);
      scheduleFlushIfNeeded();
    }
  }

  private void flush() {
    List<TCUpdatedEvent> batch;
    synchronized (monitor) {
      batch = new ArrayList<>(pendingEvents.values());
      pendingEvents.clear();
      scheduledFlush = null;
    }

    if (batch.isEmpty()) {
      return;
    }

    try {
      refreshQueueService.queueForUpdates(batch);
    } catch (RuntimeException exception) {
      LOGGER.error("Could not persist a TC insight refresh batch.", exception);
      synchronized (monitor) {
        batch.forEach(this::mergeLatest);
      }
    } finally {
      synchronized (monitor) {
        if (!pendingEvents.isEmpty()) {
          scheduleFlushIfNeeded();
        }
      }
    }
  }

  private void mergeLatest(TCUpdatedEvent event) {
    EventKey key = new EventKey(event.userId(), event.moduleId(), event.tcId());
    pendingEvents.merge(
        key,
        event,
        (current, replacement) -> replacement.updatedAt().isAfter(current.updatedAt())
            ? replacement
            : current);
  }

  private void scheduleFlushIfNeeded() {
    if (scheduledFlush != null) {
      return;
    }
    scheduledFlush = taskScheduler.schedule(
        this::flush,
        Instant.now().plusMillis(debounceMs));
  }

  private record EventKey(Long userId, Long moduleId, Long tcId) {}
}
