package com.mindmesh.backend.dto.requests.ModuleRelated;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public class CreateModuleRequestDto {

  @NotBlank
  private String courseCode;

  @NotBlank
  private String schoolSem;

  @NotEmpty
  // Cuz every module has atleast one topic
  private List<String> topics;

  public CreateModuleRequestDto() {
  }

  public String getCourseCode() {
    return courseCode;
  }

  public void setCourseCode(String courseCode) {
    this.courseCode = courseCode;
  }

  public String getSchoolSem() {
    return schoolSem;
  }

  public void setSchoolSem(String schoolSem) {
    this.schoolSem = schoolSem;
  }

  public List<String> getTopics() {
    return topics;
  }

  public void setTopics(List<String> topics) {
    this.topics = topics;
  }

}
