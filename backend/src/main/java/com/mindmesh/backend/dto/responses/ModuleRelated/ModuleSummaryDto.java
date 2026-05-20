package com.mindmesh.backend.dto.responses.ModuleRelated;

public class ModuleSummaryDto {

  private Long id;
  private String courseCode;
  private String schoolSem;

  public ModuleSummaryDto(Long id, String courseCode, String schoolSem) {
    this.id = id;
    this.courseCode = courseCode;
    this.schoolSem = schoolSem;
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
}
