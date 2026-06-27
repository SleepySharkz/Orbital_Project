package com.mindmesh.backend.dto.responses.tc;

import java.time.LocalDateTime;

public class TcSummaryResponse {

  private Long id;
  private Long moduleId;
  private String ownerUsername;
  private String courseCode;
  private String schoolSem;
  private String topic;
  private int entryCount;
  private LocalDateTime updatedAt;
  private Boolean isStale;

  public TcSummaryResponse(
      Long id,
      Long moduleId,
      String ownerUsername,
      String courseCode,
      String schoolSem,
      String topic,
      int entryCount,
      LocalDateTime updatedAt,
      Boolean isStale) {
    this.id = id;
    this.moduleId = moduleId;
    this.ownerUsername = ownerUsername;
    this.courseCode = courseCode;
    this.schoolSem = schoolSem;
    this.topic = topic;
    this.entryCount = entryCount;
    this.updatedAt = updatedAt;
    this.isStale = isStale;
  }

  public Long getId() {
    return id;
  }

  public Long getModuleId() {
    return moduleId;
  }

  public String getOwnerUsername() {
    return ownerUsername;
  }

  public String getCourseCode() {
    return courseCode;
  }

  public String getSchoolSem() {
    return schoolSem;
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

  public Boolean getIsStale() {
    return isStale;
  }
}
