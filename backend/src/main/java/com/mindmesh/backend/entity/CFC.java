package com.mindmesh.backend.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.mindmesh.backend.enums.SourceType;

import jakarta.persistence.*;

@Entity
@Table(name = "coursework_flashcards")
public class CFC {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // CFC belongs to one module.
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "module_id", nullable = false)
  private CourseModule module;

  // Tell JPA to store the enumerations as STRING
  @Enumerated(EnumType.STRING)
  @Column(name = "source_type", nullable = false)
  private SourceType sourceType;

  @Column(name = "source_title", nullable = false)
  private String sourceTitle;

  @Column(nullable = false)
  private String title;

  // Column def is text, to tell jpa that this is not just one word, it could be a
  // whole group of words
  // This summary would be ai generated
  @Column(columnDefinition = "TEXT")
  private String summary;

  @OneToMany(mappedBy = "cfc", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<CFCEntry> entries = new ArrayList<>();

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  protected CFC() {
  }

  public CFC(CourseModule module, SourceType sourceType, String sourceTitle, String title, String summary) {
    this.module = module;
    this.sourceType = sourceType;
    this.sourceTitle = sourceTitle;
    this.title = title;
    this.summary = summary;
  }

  public void addEntry(CFCEntry entry) {
    if (entry == null || entries.contains(entry)) {
      return;
    }

    if (entry.getCfc() != null && entry.getCfc() != this) {
      entry.getCfc().removeEntry(entry);
    }

    entries.add(entry);
    entry.setCfc(this);
  }

  public void removeEntry(CFCEntry entry) {
    if (entry == null) {
      return;
    }

    if (entries.remove(entry) && entry.getCfc() == this) {
      entry.setCfc(null);
    }
  }

  public Long getId() {
    return id;
  }

  public CourseModule getModule() {
    return module;
  }

  public void setModule(CourseModule module) {
    this.module = module;
  }

  public SourceType getSourceType() {
    return sourceType;
  }

  public void setSourceType(SourceType sourceType) {
    this.sourceType = sourceType;
  }

  public String getSourceTitle() {
    return sourceTitle;
  }

  public void setSourceTitle(String sourceTitle) {
    this.sourceTitle = sourceTitle;
  }

  public String getTitle() {
    return this.title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getSummary() {
    return summary;
  }

  public void setSummary(String summary) {
    this.summary = summary;
  }

  public List<CFCEntry> getEntries() {
    return entries;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

}
