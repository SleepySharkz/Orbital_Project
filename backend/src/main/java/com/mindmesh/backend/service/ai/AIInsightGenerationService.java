package com.mindmesh.backend.service.ai;

import com.mindmesh.backend.dto.ai.AIGeneratedInsightResponse;
import com.mindmesh.backend.dto.ai.AIInsightGenerationRequest;

public interface AIInsightGenerationService {
  AIGeneratedInsightResponse generateInsight(AIInsightGenerationRequest request);
}