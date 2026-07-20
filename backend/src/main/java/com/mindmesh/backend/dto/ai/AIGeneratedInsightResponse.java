package com.mindmesh.backend.dto.ai;

import java.util.List;

public class AIGeneratedInsightResponse {
  private Boolean hasUsefulLink;
  private String title;
  private String summary;
  private List<AIGeneratedInsightPoint> insights;
  private String rejectionReason;

  public AIGeneratedInsightResponse() {
  }

  public AIGeneratedInsightResponse(
      Boolean hasUsefulLink,
      String title,
      String summary,
      List<AIGeneratedInsightPoint> insights,
      String rejectionReason) {
    this.hasUsefulLink = hasUsefulLink;
    this.title = title;
    this.summary = summary;
    this.insights = insights;
    this.rejectionReason = rejectionReason;
  }

  public Boolean getHasUsefulLink() { return hasUsefulLink; }
  public String getTitle() { return title; }
  public String getSummary() { return summary; }
  public List<AIGeneratedInsightPoint> getInsights() { return insights; }
  public String getRejectionReason() { return rejectionReason; }
  public void setHasUsefulLink(Boolean value) { this.hasUsefulLink = value; }
  public void setTitle(String title) { this.title = title; }
  public void setSummary(String summary) { this.summary = summary; }
  public void setInsights(List<AIGeneratedInsightPoint> insights) { this.insights = insights; }
  public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
}