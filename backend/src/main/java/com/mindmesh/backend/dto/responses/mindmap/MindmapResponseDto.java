package com.mindmesh.backend.dto.responses.mindmap;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import com.mindmesh.backend.enums.TCInsightStatus;

public class MindmapResponseDto {

  private final Long moduleId;
  private final String courseCode;
  private final String schoolSem;
  private final List<MindmapNodeDto> nodes;
  private final List<MindmapEdgeDto> edges;

  public MindmapResponseDto(
      Long moduleId,
      String courseCode,
      String schoolSem,
      List<MindmapNodeDto> nodes,
      List<MindmapEdgeDto> edges) {
    this.moduleId = moduleId;
    this.courseCode = courseCode;
    this.schoolSem = schoolSem;
    this.nodes = nodes;
    this.edges = edges;
  }

  public Long getModuleId() {
    return moduleId;
  }

  public String getCourseCode() {
    return courseCode;
  }

  public String getSchoolSem() {
    return schoolSem;
  }

  public List<MindmapNodeDto> getNodes() {
    return nodes;
  }

  public List<MindmapEdgeDto> getEdges() {
    return edges;
  }

  public static class MindmapNodeDto {
    private final Long tcId;
    private final String topic;
    private final int entryCount;
    private final LocalDateTime updatedAt;

    public MindmapNodeDto(
        Long tcId,
        String topic,
        int entryCount,
        LocalDateTime updatedAt) {
      this.tcId = tcId;
      this.topic = topic;
      this.entryCount = entryCount;
      this.updatedAt = updatedAt;
    }

    public Long getTcId() {
      return tcId;
    }

    public String getTopic() {
      return topic;
    }

    public int getEntryCount() {
      return entryCount;
    }

    public LocalDateTime getUpdatedAt() {
      return updatedAt;
    }
  }

  public static class MindmapEdgeDto {
    private final Long insightId;
    private final Long sourceTcId;
    private final Long targetTcId;
    private final TCInsightStatus status;
    private final String title;
    private final String summary;
    private final Instant updatedAt;
    private final Boolean isRefreshing;

    public MindmapEdgeDto(
        Long insightId,
        Long sourceTcId,
        Long targetTcId,
        TCInsightStatus status,
        String title,
        String summary,
        Instant updatedAt,
        Boolean isRefreshing) {
      this.insightId = insightId;
      this.sourceTcId = sourceTcId;
      this.targetTcId = targetTcId;
      this.status = status;
      this.title = title;
      this.summary = summary;
      this.updatedAt = updatedAt;
      this.isRefreshing = isRefreshing;
    }

    public Long getInsightId() {
      return insightId;
    }

    public Long getSourceTcId() {
      return sourceTcId;
    }

    public Long getTargetTcId() {
      return targetTcId;
    }

    public TCInsightStatus getStatus() {
      return status;
    }

    public String getTitle() {
      return title;
    }

    public String getSummary() {
      return summary;
    }

    public Instant getUpdatedAt() {
      return updatedAt;
    }

    public Boolean getIsRefreshing() {
      return isRefreshing;
    }
  }
}
