package com.mindmesh.backend.service.ai;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.mindmesh.backend.dto.ai.AIGeneratedInsightPoint;
import com.mindmesh.backend.dto.ai.AIGeneratedInsightResponse;
import com.mindmesh.backend.dto.ai.AIInsightGenerationRequest;

@Service
@Profile({ "local-ai-fake", "test" })
public class FakeAIInsightGenerationService implements AIInsightGenerationService {

  @Override
  public AIGeneratedInsightResponse generateInsight(AIInsightGenerationRequest request) {
    return new AIGeneratedInsightResponse(
        true,
        request.topicA() + " and " + request.topicB(),
        "This local-development insight shows how the two selected topics can be revised together.",
        List.of(new AIGeneratedInsightPoint(
            "Shared revision connection",
            "Compare the key learning points from both Topical Cheatsheets and identify where one concept constrains or supports the other.",
            request.topicA() + " entries",
            request.topicB() + " entries")),
        null);
  }
}
