package com.mindmesh.backend.entity;

import java.time.Instant;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.mindmesh.backend.enums.CFCEntryOrigin;
import com.mindmesh.backend.enums.SourceType;

import jakarta.persistence.*;

@Entity
@Table(name = "cfc_entries")
public class CFCEntry {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "cfc_id", nullable = false)
  private CFC cfc;

  @Column(name = "request_item_id", nullable = false)
  private Long requestItemId;

  @Column(nullable = false)
  private String topic;

  @Column(name = "question_text", columnDefinition = "TEXT")
  private String questionText;

  @Column(name = "rough_note", nullable = false, columnDefinition = "TEXT")
  private String roughNote;

  @Embedded // Actual note content users will be studying out of
  private GeneratedCFCPage generatedCFCPage;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "tc_id")
  private TC tc;

  @Enumerated(EnumType.STRING)
  @Column(name = "origin", length = 24)
  private CFCEntryOrigin origin;

  @Column(name = "source_owner_username")
  private String sourceOwnerUsername;

  @Column(name = "source_shared_tc_id")
  private Long sourceSharedTcId;

  @Column(name = "source_shared_entry_id")
  private Long sourceSharedEntryId;

  @Enumerated(EnumType.STRING)
  @Column(name = "source_type_at_share", length = 32)
  private SourceType sourceTypeAtShare;

  @Column(name = "source_title_at_share")
  private String sourceTitleAtShare;

  @Column(name = "source_entry_created_at")
  private LocalDateTime sourceEntryCreatedAt;

  @Column(name = "merged_at")
  private Instant mergedAt;

  @CreationTimestamp
  private LocalDateTime createdAt;

  protected CFCEntry() {
  }

  public CFCEntry(
      CFC cfc,
      Long requestItemId,
      String topic,
      String questionText,
      String roughNote,
      GeneratedCFCPage generatedCFCPage) {
    this.cfc = cfc;
    this.requestItemId = requestItemId;
    this.topic = topic;
    this.questionText = questionText;
    this.roughNote = roughNote;
    this.generatedCFCPage = generatedCFCPage;
    this.origin = CFCEntryOrigin.OWN_GENERATED;
    if (cfc != null) {
      cfc.addEntry(this);
    }
  }

  public Long getId() {
    return id;
  }

  public CFC getCfc() {
    return cfc;
  }

  public void setCfc(CFC cfc) {
    this.cfc = cfc;
  }

  public Long getRequestItemId() {
    return requestItemId;
  }

  public void setRequestItemId(Long requestItemId) {
    this.requestItemId = requestItemId;
  }

  public String getTopic() {
    return topic;
  }

  public void setTopic(String topic) {
    this.topic = topic;
  }

  public String getQuestionText() {
    return questionText;
  }

  public void setQuestionText(String questionText) {
    this.questionText = questionText;
  }

  public String getRoughNote() {
    return roughNote;
  }

  // Original rough note
  public void setRoughNote(String roughNote) {
    this.roughNote = roughNote;
  }

  public GeneratedCFCPage getGeneratedCFCPage() {
    return generatedCFCPage;
  }

  public void setGeneratedCFCPage(GeneratedCFCPage cfcEntryContent) {
    this.generatedCFCPage = cfcEntryContent;
  }

  public TC getTc() {
    return tc;
  }

  public void setTc(TC tc) {
    this.tc = tc;
  }

  public void recordSharedOrigin(
      String sourceOwnerUsername,
      SourceType sourceTypeAtShare,
      String sourceTitleAtShare,
      Long sourceSharedTcId,
      Long sourceSharedEntryId,
      LocalDateTime sourceEntryCreatedAt,
      Instant mergedAt) {
    if (sourceOwnerUsername == null || sourceOwnerUsername.isBlank()
        || sourceSharedTcId == null || sourceSharedEntryId == null || mergedAt == null) {
      throw new IllegalArgumentException("Shared-entry provenance is required.");
    }
    origin = CFCEntryOrigin.MERGED_SHARED;
    this.sourceOwnerUsername = sourceOwnerUsername.trim();
    this.sourceTypeAtShare = sourceTypeAtShare;
    this.sourceTitleAtShare = sourceTitleAtShare;
    this.sourceSharedTcId = sourceSharedTcId;
    this.sourceSharedEntryId = sourceSharedEntryId;
    this.sourceEntryCreatedAt = sourceEntryCreatedAt;
    this.mergedAt = mergedAt;
  }

  public CFCEntryOrigin getOrigin() { return origin == null ? CFCEntryOrigin.OWN_GENERATED : origin; }
  public String getSourceOwnerUsername() { return sourceOwnerUsername; }
  public Long getSourceSharedTcId() { return sourceSharedTcId; }
  public Long getSourceSharedEntryId() { return sourceSharedEntryId; }
  public SourceType getSourceTypeAtShare() { return sourceTypeAtShare; }
  public String getSourceTitleAtShare() { return sourceTitleAtShare; }
  public LocalDateTime getSourceEntryCreatedAt() { return sourceEntryCreatedAt; }
  public Instant getMergedAt() { return mergedAt; }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }
}
