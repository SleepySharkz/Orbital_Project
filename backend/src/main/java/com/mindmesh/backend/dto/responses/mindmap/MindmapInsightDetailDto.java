package com.mindmesh.backend.dto.responses.mindmap;

import java.time.Instant;
import java.util.List;

import com.mindmesh.backend.enums.TCInsightStatus;

public class MindmapInsightDetailDto {

  private final Long insightId;
  private final Long moduleId;
  private final Long sourceTcId;
  private final Long targetTcId;
  private final String topicA;
  private final String topicB;
  private final TCInsightStatus status;
  private final String title;
  private final String summary;
  private final List<InsightPointDto> points;
  private final String rejectionReason;
  private final Instant createdAt;
  private final Instant updatedAt;
  private final Instant generatedAt;

  public MindmapInsightDetailDto(
      Long insightId,
      Long moduleId,
      Long sourceTcId,
      Long targetTcId,
      String topicA,
      String topicB,
      TCInsightStatus status,
      String title,
      String summary,
      List<InsightPointDto> points,
      String rejectionReason,
      Instant createdAt,
      Instant updatedAt,
      Instant generatedAt) {
    this.insightId = insightId;
    this.moduleId = moduleId;
    this.sourceTcId = sourceTcId;
    this.targetTcId = targetTcId;
    this.topicA = topicA;
    this.topicB = topicB;
    this.status = status;
    this.title = title;
    this.summary = summary;
    this.points = points;
    this.rejectionReason = rejectionReason;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.generatedAt = generatedAt;
  }

  public Long getInsightId() { return insightId; }
  public Long getModuleId() { return moduleId; }
  public Long getSourceTcId() { return sourceTcId; }
  public Long getTargetTcId() { return targetTcId; }
  public String getTopicA() { return topicA; }
  public String getTopicB() { return topicB; }
  public TCInsightStatus getStatus() { return status; }
  public String getTitle() { return title; }
  public String getSummary() { return summary; }
  public List<InsightPointDto> getPoints() { return points; }
  public String getRejectionReason() { return rejectionReason; }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
  public Instant getGeneratedAt() { return generatedAt; }

  public static class InsightPointDto {
    private final String heading;
    private final String explanation;
    private final String sourceTopicAReferences;
    private final String sourceTopicBReferences;
    private final Integer displayOrder;

    public InsightPointDto(
        String heading,
        String explanation,
        String sourceTopicAReferences,
        String sourceTopicBReferences,
        Integer displayOrder) {
      this.heading = heading;
      this.explanation = explanation;
      this.sourceTopicAReferences = sourceTopicAReferences;
      this.sourceTopicBReferences = sourceTopicBReferences;
      this.displayOrder = displayOrder;
    }

    public String getHeading() { return heading; }
    public String getExplanation() { return explanation; }
    public String getSourceTopicAReferences() { return sourceTopicAReferences; }
    public String getSourceTopicBReferences() { return sourceTopicBReferences; }
    public Integer getDisplayOrder() { return displayOrder; }
  }
}
