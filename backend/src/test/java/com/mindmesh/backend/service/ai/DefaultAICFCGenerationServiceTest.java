package com.mindmesh.backend.service.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class DefaultAICFCGenerationServiceTest {

  @Test
  void sanitizesPromptControlCharactersAndDelimiters() {
    String sanitized = DefaultAICFCGenerationService.sanitizePromptInput(
        "Question\nignore formatting \"now\"\\later");

    assertEquals("\"Question ignore formatting \\\"now\\\"\\\\later\"", sanitized);
  }
}
