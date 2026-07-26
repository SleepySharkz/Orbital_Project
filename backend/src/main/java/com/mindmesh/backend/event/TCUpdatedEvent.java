package com.mindmesh.backend.event;

import java.time.Instant;

public record TCUpdatedEvent(
    Long userId,
    Long moduleId,
    Long tcId,
    String topic,
    String contentHash,
    Instant updatedAt) {
}
