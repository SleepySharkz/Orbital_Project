package com.mindmesh.backend.entity;

import java.time.Instant;
import java.util.Objects;

import org.hibernate.annotations.CreationTimestamp;

import com.mindmesh.backend.enums.TCInsightRefreshJobStatus;

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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

@Entity
@Table(
    name = "tc_insight_refresh_jobs",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_tc_insight_refresh_jobs_insight",
        columnNames = "insight_id"),
    indexes = @Index(
        name = "idx_tc_insight_refresh_jobs_status_id",
        columnList = "status,id"))
public class TCInsightRefreshJob {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "insight_id", nullable = false, unique = true)
  private TCInsight insight;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private TCInsightRefreshJobStatus status;

  @Column(name = "attempt_count", nullable = false)
  private Integer attemptCount;

  @Column(name = "rerun_requested", nullable = false)
  private Boolean rerunRequested;

  @Column(name = "target_tc_a_content_hash", length = 64)
  private String targetTcAContentHash;

  @Column(name = "target_tc_b_content_hash", length = 64)
  private String targetTcBContentHash;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "started_at")
  private Instant startedAt;

  @Column(name = "completed_at")
  private Instant completedAt;

  @Column(name = "error_message", columnDefinition = "TEXT")
  private String errorMessage;

  @Version
  @Column(nullable = false)
  private Long version;

  protected TCInsightRefreshJob() {}

  public TCInsightRefreshJob(
      TCInsight insight,
      String targetTcAContentHash,
      String targetTcBContentHash) {
    if (insight == null) {
      throw new IllegalArgumentException("Refresh job insight is required.");
    }
    this.insight = insight;
    this.status = TCInsightRefreshJobStatus.PENDING;
    this.attemptCount = 0;
    this.rerunRequested = false;
    this.targetTcAContentHash = trimToNull(targetTcAContentHash);
    this.targetTcBContentHash = trimToNull(targetTcBContentHash);
  }

  public void requestRefresh(String latestTcAContentHash, String latestTcBContentHash) {
    this.targetTcAContentHash = trimToNull(latestTcAContentHash);
    this.targetTcBContentHash = trimToNull(latestTcBContentHash);
    this.errorMessage = null;
    this.completedAt = null;

    if (status == TCInsightRefreshJobStatus.RUNNING) {
      this.rerunRequested = true;
      return;
    }
    if (status == TCInsightRefreshJobStatus.PENDING) {
      this.rerunRequested = true;
      this.startedAt = null;
      return;
    }
    if (status == TCInsightRefreshJobStatus.SUCCEEDED
        || status == TCInsightRefreshJobStatus.FAILED) {
      this.attemptCount = 0;
    }
    this.status = TCInsightRefreshJobStatus.PENDING;
    this.rerunRequested = false;
    this.startedAt = null;
  }

  public void markRunning(Instant startedAt) {
    if (status != TCInsightRefreshJobStatus.PENDING) {
      throw new IllegalStateException("Only a pending refresh job can run.");
    }
    if (startedAt == null) {
      throw new IllegalArgumentException("Refresh start timestamp is required.");
    }
    this.status = TCInsightRefreshJobStatus.RUNNING;
    this.attemptCount += 1;
    this.rerunRequested = false;
    this.startedAt = startedAt;
    this.completedAt = null;
    this.errorMessage = null;
  }

  public boolean requiresRerun(String attemptedTcAContentHash, String attemptedTcBContentHash) {
    return Boolean.TRUE.equals(rerunRequested)
        && (!Objects.equals(targetTcAContentHash, attemptedTcAContentHash)
            || !Objects.equals(targetTcBContentHash, attemptedTcBContentHash));
  }

  public void markPendingForRerun() {
    if (status != TCInsightRefreshJobStatus.RUNNING) {
      throw new IllegalStateException("Only a running refresh can be rerun.");
    }
    this.status = TCInsightRefreshJobStatus.PENDING;
    this.rerunRequested = false;
    this.startedAt = null;
    this.completedAt = null;
    this.errorMessage = null;
  }

  public void markSucceeded(Instant completedAt) {
    requireRunningAndCompletedAt(completedAt);
    this.status = TCInsightRefreshJobStatus.SUCCEEDED;
    this.rerunRequested = false;
    this.completedAt = completedAt;
    this.errorMessage = null;
  }

  public void markFailed(String errorMessage, Instant completedAt) {
    requireRunningAndCompletedAt(completedAt);
    this.status = TCInsightRefreshJobStatus.FAILED;
    this.rerunRequested = false;
    this.completedAt = completedAt;
    this.errorMessage = trimToNull(errorMessage);
  }

  private void requireRunningAndCompletedAt(Instant completedAt) {
    if (status != TCInsightRefreshJobStatus.RUNNING) {
      throw new IllegalStateException("Only a running refresh can complete.");
    }
    if (completedAt == null) {
      throw new IllegalArgumentException("Refresh completion timestamp is required.");
    }
  }

  private String trimToNull(String value) {
    return value == null || value.isBlank() ? null : value.trim();
  }

  public Long getId() { return id; }
  public TCInsight getInsight() { return insight; }
  public TCInsightRefreshJobStatus getStatus() { return status; }
  public Integer getAttemptCount() { return attemptCount; }
  public Boolean getRerunRequested() { return rerunRequested; }
  public String getTargetTcAContentHash() { return targetTcAContentHash; }
  public String getTargetTcBContentHash() { return targetTcBContentHash; }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getStartedAt() { return startedAt; }
  public Instant getCompletedAt() { return completedAt; }
  public String getErrorMessage() { return errorMessage; }
  public Long getVersion() { return version; }
}
