package com.mindmesh.backend.dto.responses.cfc;

public class CFCContentDto {

  private String learningPoint;
  private String explanation;
  private String mistakePattern;
  private String reviewPrompt;

  public CFCContentDto(
      String learningPoint,
      String explanation,
      String mistakePattern,
      String reviewPrompt) {
    this.learningPoint = learningPoint;
    this.explanation = explanation;
    this.mistakePattern = mistakePattern;
    this.reviewPrompt = reviewPrompt;
  }

  public String getLearningPoint() {
    return learningPoint;
  }

  public String getExplanation() {
    return explanation;
  }

  public String getMistakePattern() {
    return mistakePattern;
  }

  public String getReviewPrompt() {
    return reviewPrompt;
  }
}
