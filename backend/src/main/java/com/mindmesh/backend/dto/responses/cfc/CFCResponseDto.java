package com.mindmesh.backend.dto.responses.cfc;

import java.time.LocalDateTime;
import java.util.List;

import com.mindmesh.backend.enums.SourceType;

public class CFCResponseDto {

  private Long id;
  private Long moduleId;
  private String courseCode;
  private String schoolSem;
  private SourceType sourceType;
  private String sourceTitle;
  private String title;
  private String summary;
  private List<CFCEntryResponseDto> entries;
  private LocalDateTime createdAt;

  public CFCResponseDto(
      Long id,
      Long moduleId,
      String courseCode,
      String schoolSem,
      SourceType sourceType,
      String sourceTitle,
      String title,
      String summary,
      List<CFCEntryResponseDto> entries,
      LocalDateTime createdAt) {
    this.id = id;
    this.moduleId = moduleId;
    this.courseCode = courseCode;
    this.schoolSem = schoolSem;
    this.sourceType = sourceType;
    this.sourceTitle = sourceTitle;
    this.title = title;
    this.summary = summary;
    this.entries = entries;
    this.createdAt = createdAt;
  }

  public Long getId() {
    return id;
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

  public SourceType getSourceType() {
    return sourceType;
  }

  public String getSourceTitle() {
    return sourceTitle;
  }

  public String getTitle() {
    return title;
  }

  public String getSummary() {
    return summary;
  }

  public List<CFCEntryResponseDto> getEntries() {
    return entries;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }
}
