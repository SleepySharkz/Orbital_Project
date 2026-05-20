package com.mindmesh.backend.dto.responses.ModuleRelated;

import java.time.LocalDateTime;
import java.util.List;

public class ModuleResponseDto {
  private Long id;
  private String courseCode;
  private String schoolSem;
  private List<String> topics;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public ModuleResponseDto(Long id, String courseCode, String schoolSem, List<String> topics,
      LocalDateTime createdAt,
      LocalDateTime updatedAt) {
    this.id = id;
    this.courseCode = courseCode;
    this.schoolSem = schoolSem;
    this.topics = topics;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public Long getId() {
    return id;
  }

  public String getCourseCode() {
    return courseCode;
  }

  public String getSchoolSem() {
    return schoolSem;
  }

  public List<String> getTopics() {
    return topics;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }
}
