package com.mindmesh.backend.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
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
  @JoinColumn(name = "tfc_id")
  private TFC tfc;

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

  public void setRoughNote(String roughNote) {
    this.roughNote = roughNote;
  }

  public GeneratedCFCPage getGeneratedCFCPage() {
    return generatedCFCPage;
  }

  public void setGeneratedCFCPage(GeneratedCFCPage cfcEntryContent) {
    this.generatedCFCPage = cfcEntryContent;
  }

  public TFC getTfc() {
    return tfc;
  }

  public void setTfc(TFC tfc) {
    this.tfc = tfc;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }
}
