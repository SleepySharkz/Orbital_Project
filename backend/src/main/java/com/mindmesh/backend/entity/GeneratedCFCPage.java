package com.mindmesh.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class GeneratedCFCPage {

  @Column(name = "learning_point", columnDefinition = "TEXT")
  private String learningPoint;

  @Column(name = "explanation", columnDefinition = "TEXT")
  private String explanation;

  @Column(name = "mistake_pattern", columnDefinition = "TEXT")
  private String mistakePattern;

  @Column(name = "review_prompt", columnDefinition = "TEXT")
  private String reviewPrompt;

  protected GeneratedCFCPage() {
  }

  public GeneratedCFCPage(
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
