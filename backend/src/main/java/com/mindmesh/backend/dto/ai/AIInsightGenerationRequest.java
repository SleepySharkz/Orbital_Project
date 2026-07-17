package com.mindmesh.backend.dto.ai;

import java.util.List;

public record AIInsightGenerationRequest(
    String courseCode,
    String schoolSem,
    String topicA,
    String topicB,
    List<AIInsightEntryInput> entriesA,
    List<AIInsightEntryInput> entriesB,
    String tcAContentHash,
    String tcBContentHash) {
}
