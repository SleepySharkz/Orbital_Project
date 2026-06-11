package com.mindmesh.backend.service.ai;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.mindmesh.backend.dto.ai.AICFCGenerationRequest;
import com.mindmesh.backend.dto.ai.AICFCGenerationRequestItem;
import com.mindmesh.backend.dto.ai.AIGeneratedCFCEntry;
import com.mindmesh.backend.dto.ai.AIGeneratedCFCResponse;
import com.mindmesh.backend.dto.ai.AIImageInput;
import com.mindmesh.backend.dto.requests.cfc.CreateCFCRequestDto;
import com.mindmesh.backend.dto.requests.cfc.QnNotePairDto;
import com.mindmesh.backend.entity.CourseModule;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
@Profile("real-ai")
public class DefaultAICFCGenerationService implements AICFCGenerationService {

  private final AIProviderClient aiProviderClient;
  private final ObjectMapper objectMapper;

  public DefaultAICFCGenerationService(
      AIProviderClient aiProviderClient,
      ObjectMapper objectMapper) {
    this.aiProviderClient = aiProviderClient;
    this.objectMapper = objectMapper;
  }

  @Override
  public AIGeneratedCFCResponse generateCFC(
      CourseModule module,
      CreateCFCRequestDto requestDto,
      Map<String, MultipartFile> imageFileMap) {
    Map<String, MultipartFile> uploadedFiles = imageFileMap == null ? Map.of() : imageFileMap;
    AICFCGenerationRequest generationRequest = buildGenerationRequest(module, requestDto, uploadedFiles);
    String prompt = buildPrompt(generationRequest);
    String rawProviderResponse = aiProviderClient.generate(generationRequest, prompt);
    String generatedJson = extractGeneratedJson(rawProviderResponse);
    AIGeneratedCFCResponse generatedCFC = parseGeneratedCFC(generatedJson);

    validateGeneratedCFC(generatedCFC, requestDto.getItems());
    return generatedCFC;
  }

  private AICFCGenerationRequest buildGenerationRequest(
      CourseModule module,
      CreateCFCRequestDto requestDto,
      Map<String, MultipartFile> imageFileMap) {
    List<AICFCGenerationRequestItem> items = requestDto
        .getItems()
        .stream()
        .map(item -> new AICFCGenerationRequestItem(
            item.getItemId(),
            item.getTopic(),
            item.getQuestionText(),
            item.getRoughNote(),
            buildImageInputs(item, imageFileMap)))
        .toList();

    return new AICFCGenerationRequest(
        module.getCourseCode(),
        module.getSchoolSem(),
        requestDto.getFlashcardHeader().getSourceType().name(),
        requestDto.getFlashcardHeader().getSourceTitle(),
        items);
  }

  private List<AIImageInput> buildImageInputs(
      QnNotePairDto item,
      Map<String, MultipartFile> imageFileMap) {
    if (item.getImageKeys() == null || item.getImageKeys().isEmpty()) {
      return List.of();
    }

    return item
        .getImageKeys()
        .stream()
        .map(imageKey -> toAIImageInput(imageKey, imageFileMap.get(imageKey)))
        .toList();
  }

  private AIImageInput toAIImageInput(String imageKey, MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_GATEWAY,
          "Could not find uploaded image for AI generation: " + imageKey);
    }

    try {
      return AIImageInput.fromMultipartFile(imageKey, file);
    } catch (IOException exception) {
      throw new ResponseStatusException(
          HttpStatus.BAD_GATEWAY,
          "Could not read uploaded image for AI generation: " + imageKey,
          exception);
    }
  }

  private String buildPrompt(AICFCGenerationRequest request) {
    StringBuilder prompt = new StringBuilder();
    prompt.append("You generate Coursework Flashcard (CFC) study content for MindMesh.\n");
    prompt.append("Return valid JSON only. Do not include Markdown or commentary.\n\n");
    prompt.append("Output shape:\n");
    prompt.append("{\n");
    prompt.append("  \"title\": \"Short title for the CFC\",\n");
    prompt.append("  \"summary\": \"2-4 sentence summary of what this CFC helps review\",\n");
    prompt.append("  \"entries\": [\n");
    prompt.append("    {\n");
    prompt.append("      \"requestItemId\": 1,\n");
    prompt.append("      \"flashcardQuestion\": \"Concise study question\",\n");
    prompt.append("      \"flashcardNoteContent\": \"- Point-form note content with short explanatory sentences\\n- Include extra learning points only when helpful\"\n");
    prompt.append("    }\n");
    prompt.append("  ]\n");
    prompt.append("}\n\n");
    prompt.append("Rules:\n");
    prompt.append("- Include exactly one entry for every requestItemId below.\n");
    prompt.append("- Do not invent or omit requestItemIds.\n");
    prompt.append("- Use the rough note to infer what the student misunderstood or needs to remember.\n");
    prompt.append("- If images are attached, inspect them as source material for that requestItemId.\n");
    prompt.append("- flashcardQuestion must be a concise revision-style question that targets the core concept being tested.\n");
    prompt.append("- Rewrite unclear raw submissions into a cleaner study question, but preserve the original technical meaning.\n");
    prompt.append("- Avoid vague questions and avoid yes/no questions unless the concept genuinely requires them.\n");
    prompt.append("- flashcardNoteContent must be in point form.\n");
    prompt.append("- Write 3 to 6 bullet points unless the material is extremely small.\n");
    prompt.append("- Each bullet should usually be a short explanatory sentence, not isolated keywords.\n");
    prompt.append("- The notes should be concise, but not skeletal or overly compressed.\n");
    prompt.append("- Include the main reasoning, edge cases, clarifications, or common traps only when they improve revision value.\n");
    prompt.append("- Keep terminology technically accurate to the course topic.\n");
    prompt.append("- The note content should read like the answer to the flashcardQuestion.\n");
    prompt.append("- Do not output labeled subsections such as Learning Point, Explanation, Mistake Pattern, or Review Prompt.\n\n");
    prompt.append("Quality examples:\n");
    prompt.append("- Good flashcardQuestion: \"Why is the inorder successor used during BST deletion in this case?\"\n");
    prompt.append("- Bad flashcardQuestion: \"BST deletion\"\n");
    prompt.append("- Good flashcardNoteContent bullet: \"Handle the empty-tree base case first so deletion logic does not recurse into a null subtree.\"\n");
    prompt.append("- Bad flashcardNoteContent bullet: \"empty tree case\"\n\n");
    prompt.append("Course: ").append(request.getCourseCode()).append("\n");
    prompt.append("Semester: ").append(request.getSchoolSem()).append("\n");
    prompt.append("Source type: ").append(request.getSourceType()).append("\n");
    prompt.append("Source title: ").append(request.getSourceTitle()).append("\n\n");
    prompt.append("Items:\n");

    for (AICFCGenerationRequestItem item : request.getItems()) {
      prompt.append("- requestItemId: ").append(item.getRequestItemId()).append("\n");
      prompt.append("  topic: ").append(item.getTopic()).append("\n");
      prompt.append("  questionText: ").append(nullToEmpty(item.getQuestionText())).append("\n");
      prompt.append("  roughNote: ").append(nullToEmpty(item.getRoughNotes())).append("\n");
      prompt.append("  imageCount: ").append(item.getImages().size()).append("\n");

      for (AIImageInput image : item.getImages()) {
        prompt.append("  image: ")
            .append(image.getImageKey())
            .append(" / ")
            .append(image.getFileName())
            .append("\n");
      }
    }

    return prompt.toString();
  }

  private String extractGeneratedJson(String rawProviderResponse) {
    try {
      JsonNode root = objectMapper.readTree(rawProviderResponse);
      JsonNode parts = root
          .path("candidates")
          .path(0)
          .path("content")
          .path("parts");

      if (!parts.isArray() || parts.size() == 0) {
        throw invalidAIResponse("AI provider response did not contain generated text.");
      }

      StringBuilder text = new StringBuilder();
      for (JsonNode part : parts) {
        JsonNode textNode = part.path("text");
        if (textNode.isTextual()) {
          text.append(textNode.asText());
        }
      }

      String generatedText = stripCodeFence(text.toString());
      if (isBlank(generatedText)) {
        throw invalidAIResponse("AI provider response text was empty.");
      }

      return generatedText;
    } catch (RuntimeException exception) {
      throw new ResponseStatusException(
          HttpStatus.BAD_GATEWAY,
          "Could not parse AI provider response.",
          exception);
    }
  }

  private AIGeneratedCFCResponse parseGeneratedCFC(String generatedJson) {
    try {
      return objectMapper.readValue(generatedJson, AIGeneratedCFCResponse.class);
    } catch (RuntimeException exception) {
      throw new ResponseStatusException(
          HttpStatus.BAD_GATEWAY,
          "AI generation did not return the expected JSON shape.",
          exception);
    }
  }

  private void validateGeneratedCFC(
      AIGeneratedCFCResponse generatedCFC,
      List<QnNotePairDto> requestItems) {
    if (generatedCFC == null) {
      throw invalidAIResponse("AI generation returned no CFC content.");
    }

    if (isBlank(generatedCFC.getTitle()) || isBlank(generatedCFC.getSummary())) {
      throw invalidAIResponse("AI generation returned a blank title or summary.");
    }

    if (generatedCFC.getEntries() == null || generatedCFC.getEntries().size() != requestItems.size()) {
      throw invalidAIResponse("AI generation returned the wrong number of entries.");
    }

    Set<Long> expectedItemIds = new HashSet<>();
    for (QnNotePairDto item : requestItems) {
      expectedItemIds.add(item.getItemId());
    }

    Set<Long> seenItemIds = new HashSet<>();
    for (AIGeneratedCFCEntry entry : generatedCFC.getEntries()) {
      if (entry.getRequestItemId() == null || !expectedItemIds.contains(entry.getRequestItemId())) {
        throw invalidAIResponse("AI generation returned an unknown requestItemId.");
      }

      if (!seenItemIds.add(entry.getRequestItemId())) {
        throw invalidAIResponse("AI generation returned duplicate requestItemIds.");
      }

      if (isBlank(entry.getFlashcardQuestion())
          || isBlank(entry.getFlashcardNoteContent())) {
        throw invalidAIResponse("AI generation returned blank entry content.");
      }
    }
  }

  private ResponseStatusException invalidAIResponse(String reason) {
    return new ResponseStatusException(HttpStatus.BAD_GATEWAY, reason);
  }

  private String stripCodeFence(String value) {
    String trimmed = value == null ? "" : value.trim();

    if (!trimmed.startsWith("```")) {
      return trimmed;
    }

    int firstNewline = trimmed.indexOf('\n');
    int lastFence = trimmed.lastIndexOf("```");

    if (firstNewline == -1 || lastFence <= firstNewline) {
      return trimmed;
    }

    return trimmed.substring(firstNewline + 1, lastFence).trim();
  }

  private String nullToEmpty(String value) {
    return value == null ? "" : value;
  }

  private boolean isBlank(String value) {
    return value == null || value.isBlank();
  }
}
