package com.mindmesh.backend.dto.responses.ModuleRelated;

import java.util.List;

public class ModuleTopicsResponseDto {

  private Long moduleId;
  private String courseCode;
  private List<String> topics;

  public ModuleTopicsResponseDto(Long moduleId, String courseCode, List<String> topics) {
    this.moduleId = moduleId;
    this.courseCode = courseCode;
    this.topics = topics;
  }

  public Long getModuleId() {
    return moduleId;
  }

  public String getCourseCode() {
    return courseCode;
  }

  public List<String> getTopics() {
    return topics;
  }
}
