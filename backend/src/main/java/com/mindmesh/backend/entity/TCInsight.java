package com.mindmesh.backend.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.mindmesh.backend.enums.TCInsightStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "tc_insights",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_tc_insights_user_module_pair",
            columnNames = { "user_id", "module_id", "tc_a_id", "tc_b_id" })
    },
    indexes = {
        @Index(name = "idx_tc_insights_user_module", columnList = "user_id,module_id"),
        @Index(name = "idx_tc_insights_tc_a", columnList = "tc_a_id"),
        @Index(name = "idx_tc_insights_tc_b", columnList = "tc_b_id")
    })
public class TCInsight {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "module_id", nullable = false)
  private CourseModule module;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "tc_a_id", nullable = false)
  private TC tcA;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "tc_b_id", nullable = false)
  private TC tcB;

  @Column(name = "topic_a", nullable = false)
  private String topicA;

  @Column(name = "topic_b", nullable = false)
  private String topicB;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private TCInsightStatus status;

  @Column
  private String title;

  @Column(columnDefinition = "TEXT")
  private String summary;

  @OneToMany(mappedBy = "insight", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("displayOrder ASC, id ASC")
  private List<TCInsightPoint> points = new ArrayList<>();

  @Column(name = "rejection_reason", columnDefinition = "TEXT")
  private String rejectionReason;

  @Column(name = "tc_a_content_hash_at_generation", length = 64)
  private String tcAContentHashAtGeneration;

  @Column(name = "tc_b_content_hash_at_generation", length = 64)
  private String tcBContentHashAtGeneration;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Column(name = "generated_at")
  private Instant generatedAt;

  @Column(name = "last_refresh_started_at")
  private Instant lastRefreshStartedAt;

  @Column(name = "last_refresh_completed_at")
  private Instant lastRefreshCompletedAt;

  protected TCInsight() {
  }

  public TCInsight(User user, CourseModule module, TC firstTc, TC secondTc) {
    if (user == null || module == null || firstTc == null || secondTc == null) {
      throw new IllegalArgumentException("Insight relationships are required.");
    }

    if (firstTc.getId() == null || secondTc.getId() == null) {
      throw new IllegalArgumentException("Insight TCs must already be persisted.");
    }

    if (firstTc.getId().equals(secondTc.getId())) {
      throw new IllegalArgumentException("An insight requires two different TCs.");
    }

    this.user = user;
    this.module = module;

    if (firstTc.getId() < secondTc.getId()) {
      assignCanonicalPair(firstTc, secondTc);
    } else {
      assignCanonicalPair(secondTc, firstTc);
    }

    this.status = TCInsightStatus.GENERATING;
  }

  public void markReady(
      String title,
      String summary,
      List<TCInsightPoint> generatedPoints,
      String tcAContentHash,
      String tcBContentHash,
      Instant generatedAt) {
    if (isBlank(title) || isBlank(summary)) {
      throw new IllegalArgumentException("Ready insight title and summary are required.");
    }

    if (generatedPoints == null || generatedPoints.isEmpty()) {
      throw new IllegalArgumentException("A ready insight requires at least one point.");
    }

    if (generatedAt == null) {
      throw new IllegalArgumentException("Insight generation timestamp is required.");
    }

    this.title = title.trim();
    this.summary = summary.trim();
    replacePoints(generatedPoints);
    this.rejectionReason = null;
    this.tcAContentHashAtGeneration = trimToNull(tcAContentHash);
    this.tcBContentHashAtGeneration = trimToNull(tcBContentHash);
    this.generatedAt = generatedAt;
    this.status = TCInsightStatus.READY;
  }

  public void markNoUsefulLink(
      String rejectionReason,
      String tcAContentHash,
      String tcBContentHash,
      Instant generatedAt) {
    if (isBlank(rejectionReason)) {
      throw new IllegalArgumentException("A no-link insight requires a rejection reason.");
    }

    if (generatedAt == null) {
      throw new IllegalArgumentException("Insight generation timestamp is required.");
    }

    this.title = null;
    this.summary = null;
    replacePoints(List.of());
    this.rejectionReason = rejectionReason.trim();
    this.tcAContentHashAtGeneration = trimToNull(tcAContentHash);
    this.tcBContentHashAtGeneration = trimToNull(tcBContentHash);
    this.generatedAt = generatedAt;
    this.status = TCInsightStatus.NO_USEFUL_LINK;
  }

    public void markGenerationFailed(String failureReason) {
    if (isBlank(failureReason)) {
      throw new IllegalArgumentException("A generation failure reason is required.");
    }

    this.title = null;
    this.summary = null;
    replacePoints(List.of());
    this.rejectionReason = failureReason.trim();
    this.status = TCInsightStatus.GENERATION_FAILED;
  }

  public void restartGeneration() {
    if (status != TCInsightStatus.GENERATION_FAILED) {
      throw new IllegalStateException("Only a failed generation can be restarted.");
    }

    this.title = null;
    this.summary = null;
    replacePoints(List.of());
    this.rejectionReason = null;
    this.status = TCInsightStatus.GENERATING;
  }

  public void markRefreshing(Instant startedAt) {
    if (startedAt == null) {
      throw new IllegalArgumentException("Refresh start timestamp is required.");
    }

    this.lastRefreshStartedAt = startedAt;
    this.status = TCInsightStatus.REFRESHING;
  }

  public void markRefreshFailed(Instant completedAt) {
    if (completedAt == null) {
      throw new IllegalArgumentException("Refresh completion timestamp is required.");
    }

    this.lastRefreshCompletedAt = completedAt;
    this.status = TCInsightStatus.REFRESH_FAILED;
  }

  public void addPoint(TCInsightPoint point) {
    if (point == null || points.contains(point)) {
      return;
    }

    if (point.getInsight() != null && point.getInsight() != this) {
      throw new IllegalArgumentException("Insight point already belongs to another insight.");
    }

    points.add(point);
    point.setInsight(this);
  }

  public void removePoint(TCInsightPoint point) {
    if (point == null) {
      return;
    }

    if (points.remove(point) && point.getInsight() == this) {
      point.setInsight(null);
    }
  }

  private void replacePoints(List<TCInsightPoint> replacementPoints) {
    for (TCInsightPoint point : new ArrayList<>(points)) {
      removePoint(point);
    }

    for (TCInsightPoint point : new ArrayList<>(replacementPoints)) {
      addPoint(point);
    }
  }

  private void assignCanonicalPair(TC lowerIdTc, TC higherIdTc) {
    this.tcA = lowerIdTc;
    this.tcB = higherIdTc;
    this.topicA = lowerIdTc.getTopic();
    this.topicB = higherIdTc.getTopic();
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

  public User getUser() {
    return user;
  }

  public CourseModule getModule() {
    return module;
  }

  public TC getTcA() {
    return tcA;
  }

  public TC getTcB() {
    return tcB;
  }

  public String getTopicA() {
    return topicA;
  }

  public String getTopicB() {
    return topicB;
  }

  public TCInsightStatus getStatus() {
    return status;
  }

  public String getTitle() {
    return title;
  }

  public String getSummary() {
    return summary;
  }

  public List<TCInsightPoint> getPoints() {
    return points;
  }

  public String getRejectionReason() {
    return rejectionReason;
  }

  public String getTcAContentHashAtGeneration() {
    return tcAContentHashAtGeneration;
  }

  public String getTcBContentHashAtGeneration() {
    return tcBContentHashAtGeneration;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public Instant getGeneratedAt() {
    return generatedAt;
  }

  public Instant getLastRefreshStartedAt() {
    return lastRefreshStartedAt;
  }

  public Instant getLastRefreshCompletedAt() {
    return lastRefreshCompletedAt;
  }
}
