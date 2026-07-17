package com.mindmesh.backend.dto.ai;

public class AIGeneratedInsightPoint {
  private String heading;
  private String explanation;
  private String sourceTopicAReferences;
  private String sourceTopicBReferences;

  public AIGeneratedInsightPoint() {
  }

  public AIGeneratedInsightPoint(
      String heading,
      String explanation,
      String sourceTopicAReferences,
      String sourceTopicBReferences) {
    this.heading = heading;
    this.explanation = explanation;
    this.sourceTopicAReferences = sourceTopicAReferences;
    this.sourceTopicBReferences = sourceTopicBReferences;
  }

  public String getHeading() { return heading; }
  public String getExplanation() { return explanation; }
  public String getSourceTopicAReferences() { return sourceTopicAReferences; }
  public String getSourceTopicBReferences() { return sourceTopicBReferences; }
  public void setHeading(String heading) { this.heading = heading; }
  public void setExplanation(String explanation) { this.explanation = explanation; }
  public void setSourceTopicAReferences(String value) { this.sourceTopicAReferences = value; }
  public void setSourceTopicBReferences(String value) { this.sourceTopicBReferences = value; }
}