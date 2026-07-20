package com.mindmesh.backend.service;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;

import org.springframework.stereotype.Component;

import com.mindmesh.backend.dto.ai.AIInsightEntryInput;
import com.mindmesh.backend.dto.ai.AIInsightGenerationRequest;
import com.mindmesh.backend.entity.CFCEntry;
import com.mindmesh.backend.entity.GeneratedCFCPage;
import com.mindmesh.backend.entity.TC;
import com.mindmesh.backend.entity.TCInsight;

@Component
public class TCInsightInputFactory {

  public AIInsightGenerationRequest build(TCInsight insight, TC tcA, TC tcB) {
    requirePairMember(insight.getTcA().getId(), tcA);
    requirePairMember(insight.getTcB().getId(), tcB);

    List<AIInsightEntryInput> entriesA = toAIEntries(tcA);
    List<AIInsightEntryInput> entriesB = toAIEntries(tcB);
    if (entriesA.isEmpty() || entriesB.isEmpty()) {
      throw new IllegalStateException("Insight input TC is no longer available.");
    }

    return new AIInsightGenerationRequest(
        insight.getModule().getCourseCode(),
        insight.getModule().getSchoolSem(),
        insight.getTopicA(),
        insight.getTopicB(),
        entriesA,
        entriesB,
        contentHash(tcA.getTopic(), entriesA),
        contentHash(tcB.getTopic(), entriesB));
  }

  public String contentHash(TC tc) {
    return contentHash(tc.getTopic(), toAIEntries(tc));
  }

  private List<AIInsightEntryInput> toAIEntries(TC tc) {
    return tc.getEntries().stream()
        .sorted(Comparator.comparing(CFCEntry::getId))
        .map(this::toAIEntry)
        .toList();
  }

  private AIInsightEntryInput toAIEntry(CFCEntry entry) {
    GeneratedCFCPage generated = entry.getGeneratedCFCPage();
    return new AIInsightEntryInput(
        entry.getId(),
        generated == null ? null : generated.getFlashcardQuestion(),
        generated == null ? null : generated.getFlashcardNoteContent(),
        entry.getQuestionText(),
        entry.getRoughNote(),
        entry.getCfc().getSourceType().name(),
        entry.getCfc().getSourceTitle());
  }

  private String contentHash(String topic, List<AIInsightEntryInput> entries) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      updateDigest(digest, topic);
      for (AIInsightEntryInput entry : entries) {
        updateDigest(digest, entry.entryId() == null ? null : entry.entryId().toString());
        updateDigest(digest, entry.generatedQuestion());
        updateDigest(digest, entry.generatedNote());
        updateDigest(digest, entry.originalQuestion());
        updateDigest(digest, entry.roughNote());
        updateDigest(digest, entry.sourceType());
        updateDigest(digest, entry.sourceTitle());
      }
      return HexFormat.of().formatHex(digest.digest());
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException("SHA-256 is unavailable.", exception);
    }
  }

  private void updateDigest(MessageDigest digest, String value) {
    if (value == null) {
      digest.update(ByteBuffer.allocate(Integer.BYTES).putInt(-1).array());
      return;
    }
    byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
    digest.update(ByteBuffer.allocate(Integer.BYTES).putInt(bytes.length).array());
    digest.update(bytes);
  }

  private void requirePairMember(Long expectedId, TC tc) {
    if (tc == null || tc.getId() == null || !expectedId.equals(tc.getId())) {
      throw new IllegalStateException("Insight input TC is no longer available.");
    }
  }
}
