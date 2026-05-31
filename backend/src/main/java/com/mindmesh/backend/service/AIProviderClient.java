package com.mindmesh.backend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@Profile("real-ai")
public class AIProviderClient {

  private final RestClient restClient;
  private final AIProviderProperties properties;

  public AIProviderClient(AIProviderProperties properties) {
    this.restClient = RestClient.create(properties.getBaseUrl());
    this.properties = properties;
  }

  public String generate(AICFCGenerationRequest generationRequest, String prompt) {
    Map<String, Object> requestBody = Map.of(
        "contents", List.of(
            Map.of("parts", buildGeminiParts(generationRequest, prompt))),
        "generationConfig", Map.of(
            "temperature", 0.2,
            "responseMimeType", "application/json"));

    return restClient.post()
        .uri("/v1beta/models/{model}:generateContent", properties.getModel())
        .header("x-goog-api-key", properties.getApiKey())
        .body(requestBody)
        .retrieve()
        .body(String.class);
  }

  private List<Map<String, Object>> buildGeminiParts(
      AICFCGenerationRequest generationRequest,
      String prompt) {
    List<Map<String, Object>> parts = new ArrayList<>();
    parts.add(Map.of("text", prompt));

    for (AICFCGenerationRequestItem item : generationRequest.getItems()) {
      for (AIImageInput image : item.getImages()) {
        parts.add(Map.of(
            "text",
            "Image input for requestItemId " + item.getRequestItemId()
                + ", imageKey " + image.getImageKey()
                + ", fileName " + image.getFileName()));
        parts.add(Map.of(
            "inline_data",
            Map.of(
                "mime_type", getMimeType(image),
                "data", image.getBase64Data())));
      }
    }

    return parts;
  }

  private String getMimeType(AIImageInput image) {
    if (image.getContentType() == null || image.getContentType().isBlank()) {
      return "image/png";
    }

    return image.getContentType();
  }
}
