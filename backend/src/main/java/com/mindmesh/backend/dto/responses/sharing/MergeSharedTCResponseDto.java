package com.mindmesh.backend.dto.responses.sharing;

import java.time.Instant;

import com.mindmesh.backend.enums.SharedTCStatus;

public record MergeSharedTCResponseDto(
    Long ownedTcId,
    Long sharedTcId,
    int mergedEntryCount,
    SharedTCStatus status,
    Instant mergedAt) {
}
