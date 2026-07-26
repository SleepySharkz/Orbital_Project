package com.mindmesh.backend.service;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.mindmesh.backend.event.TCUpdatedEvent;

@Component
public class TCUpdatedEventListener {

  private final TCInsightRefreshEventBuffer refreshEventBuffer;

  public TCUpdatedEventListener(TCInsightRefreshEventBuffer refreshEventBuffer) {
    this.refreshEventBuffer = refreshEventBuffer;
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onTcUpdated(TCUpdatedEvent event) {
    refreshEventBuffer.enqueue(event);
  }
}
