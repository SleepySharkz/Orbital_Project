package com.mindmesh.backend.dto.ai;

public record AIInsightEntryInput(
    Long entryId,
    String generatedQuestion,
    String generatedNote,
    String originalQuestion,
    String roughNote,
    String sourceType,
    String sourceTitle) {
}