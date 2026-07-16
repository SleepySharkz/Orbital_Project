package com.mindmesh.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tc_insight_points")
public class TCInsightPoint {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "insight_id", nullable = false)
  private TCInsight insight;

  @Column(nullable = false)
  private String heading;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String explanation;

  @Column(name = "source_topic_a_references", columnDefinition = "TEXT")
  private String sourceTopicAReferences;

  @Column(name = "source_topic_b_references", columnDefinition = "TEXT")
  private String sourceTopicBReferences;

  @Column(name = "display_order", nullable = false)
  private Integer displayOrder;

  protected TCInsightPoint() {
  }

  public TCInsightPoint(
      String heading,
      String explanation,
      String sourceTopicAReferences,
      String sourceTopicBReferences,
      Integer displayOrder
    ) {
    if (isBlank(heading) || isBlank(explanation)) {
      throw new IllegalArgumentException("Insight point heading and explanation are required.");
    }

    if (displayOrder == null || displayOrder < 0) {
      throw new IllegalArgumentException("Insight point display order must be non-negative.");
    }

    this.heading = heading.trim();
    this.explanation = explanation.trim();
    this.sourceTopicAReferences = trimToNull(sourceTopicAReferences);
    this.sourceTopicBReferences = trimToNull(sourceTopicBReferences);
    this.displayOrder = displayOrder;
  }

  void setInsight(TCInsight insight) {
    this.insight = insight;
  }

  private boolean isBlank(String value) {
    return value == null || value.isBlank();
  }

  private String trimToNull(String value) {
    return isBlank(value) ? null : value.trim();
  }

  public Long getId() {
    return id;
  }

  public TCInsight getInsight() {
    return insight;
  }

  public String getHeading() {
    return heading;
  }

  public String getExplanation() {
    return explanation;
  }

  public String getSourceTopicAReferences() {
    return sourceTopicAReferences;
  }

  public String getSourceTopicBReferences() {
    return sourceTopicBReferences;
  }

  public Integer getDisplayOrder() {
    return displayOrder;
  }
}
