package com.mindmesh.backend.dto.responses.cfc;

public class SourceMaterialDto {

  private String questionText;
  private String roughNote;

  public SourceMaterialDto(
      String questionText,
      String roughNote) {
    this.questionText = questionText;
    this.roughNote = roughNote;
  }

  public String getQuestionText() {
    return questionText;
  }

  public String getRoughNote() {
    return roughNote;
  }
}
