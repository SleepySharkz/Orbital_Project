package com.mindmesh.backend.service;

public class AIGeneratedCFCEntry {
    //this is temporary, we will change the fields according to the shape
    //we want the output to be
    private Long requestItemId;
    private String learningPoint;
    private String explanation;
    private String mistakePattern;
    private String reviewPrompt;

    public AIGeneratedCFCEntry() {
    }

    public AIGeneratedCFCEntry(
        Long requestItemId,
        String learningPoint,
        String explanation,
        String mistakePattern,
        String reviewPrompt) {
            this.requestItemId = requestItemId;
            this.learningPoint = learningPoint;
            this.explanation = explanation;
            this.mistakePattern = mistakePattern;
            this.reviewPrompt = reviewPrompt;
        }
    
    //Getters
    public Long getRequestItemId() {
        return this.requestItemId;
    }

    public String getLearningPoint() {
        return this.learningPoint;
    }

    public String getExplanation() {
        return this.explanation;
    }

    public String getMistakePattern() {
        return this.mistakePattern;
    }

    public String getReviewPrompt() {
        return this.reviewPrompt;
    }

    //Setters
    public void setRequestItemId(Long requestItemId) {
        this.requestItemId = requestItemId;
    }

    public void setLearningPoint(String newLearningPoint) {
        this.learningPoint = newLearningPoint;
    }

    public void setExplanation(String newExplanation) {
        this.explanation = newExplanation;
    }

    public void setMistakePattern(String newMistakePattern) {
        this.mistakePattern = newMistakePattern;
    }

    public void setReviewPrompt(String newReviewPrompt) {
        this.reviewPrompt = newReviewPrompt;
    }
}
