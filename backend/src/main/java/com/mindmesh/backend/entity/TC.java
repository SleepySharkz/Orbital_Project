package com.mindmesh.backend.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "topical_flashcards", uniqueConstraints = {
    @UniqueConstraint(columnNames = { "owner_id", "module_id", "topic" })
})
public class TC {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // Source of truth for a TC is (module (+ owner since module already has
  // ownership with user), topic).
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "module_id", nullable = false)
  private CourseModule module;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "owner_id", nullable = false)
  private User owner;

  @Column(nullable = false)
  private String topic;

  @OneToMany(mappedBy = "tc")
  private List<CFCEntry> entries = new ArrayList<>();

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  protected TC() {
  }

  public TC(CourseModule module, User owner, String topic) {
    this.module = module;
    this.owner = owner;
    this.topic = topic;
  }

  public void addEntry(CFCEntry entry) {
    if (entry == null || entries.contains(entry)) {
      return;
    }

    if (entry.getTc() != null && entry.getTc() != this) {
      entry.getTc().removeEntry(entry);
    }

    entries.add(entry);
    entry.setTc(this);
  }

  public void removeEntry(CFCEntry entry) {
    if (entry == null) {
      return;
    }

    if (entries.remove(entry) && entry.getTc() == this) {
      entry.setTc(null);
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

  public User getOwner() {
    return owner;
  }

  public void setOwner(User owner) {
    this.owner = owner;
  }

  public String getTopic() {
    return topic;
  }

  public void setTopic(String topic) {
    this.topic = topic;
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
