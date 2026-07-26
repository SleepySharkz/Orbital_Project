package com.mindmesh.backend.service;

import java.time.Instant;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.mindmesh.backend.entity.TC;
import com.mindmesh.backend.event.TCUpdatedEvent;

@Component
public class TCUpdateEventPublisher {

  private final ApplicationEventPublisher applicationEventPublisher;
  private final TCInsightInputFactory inputFactory;

  public TCUpdateEventPublisher(
      ApplicationEventPublisher applicationEventPublisher,
      TCInsightInputFactory inputFactory) {
    this.applicationEventPublisher = applicationEventPublisher;
    this.inputFactory = inputFactory;
  }

  public void publishUpdated(TC tc) {
    if (tc == null
        || tc.getId() == null
        || tc.getOwner() == null
        || tc.getOwner().getId() == null
        || tc.getModule() == null
        || tc.getModule().getId() == null) {
      throw new IllegalArgumentException("A persisted owned TC is required for TC_UPDATED.");
    }

    applicationEventPublisher.publishEvent(new TCUpdatedEvent(
        tc.getOwner().getId(),
        tc.getModule().getId(),
        tc.getId(),
        tc.getTopic(),
        inputFactory.contentHash(tc),
        Instant.now()));
  }
}
