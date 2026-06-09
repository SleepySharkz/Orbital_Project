package com.mindmesh.backend.dto.requests.cfc;

import jakarta.validation.constraints.NotBlank;

public class UpdateCFCEntryContentRequestDto {
    @NotBlank(message = "Learning point is required")
    private String learningPoint;

    @NotBlank(message = "Explanation is required")
    private String explanation;

    @NotBlank(message = "Mistake pattern is required")
    private String mistakePattern;

    @NotBlank(message = "Review prompt is required")
    private String reviewPrompt;

    public UpdateCFCEntryContentRequestDto() {
    }

    public String getLearningPoint() {
        return this.learningPoint;
    }

    public void setLearningPoint(String learningPoint) {
        this.learningPoint = learningPoint;
    }

    public String getExplanation() {
        return this.explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public String getMistakePattern() {
        return this.mistakePattern;
    }

    public void setMistakePattern(String mistakePattern) {
        this.mistakePattern = mistakePattern;
    }

    public String getReviewPrompt() {
        return this.reviewPrompt;
    }

    public void setReviewPrompt(String reviewPrompt) {
        this.reviewPrompt = reviewPrompt;
    }
}