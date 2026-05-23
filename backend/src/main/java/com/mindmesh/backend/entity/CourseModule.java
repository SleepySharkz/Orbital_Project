package com.mindmesh.backend.entity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;

@Entity
@Table(name = "course_modules", uniqueConstraints = {
    @UniqueConstraint(columnNames = {
        "user_id",
        "course_code",
        "school_sem" })
})
public class CourseModule {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // Many of the course modules can have the same user
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user; // Every course module MUST have a user

  @Column(name = "course_code", nullable = false)
  private String courseCode;

  @Column(name = "school_sem", nullable = false)
  private String schoolSem;

  @OneToMany(mappedBy = "courseModule", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ModuleTopic> topics = new ArrayList<>();

  // These may be used in the future if we want to edit the flashcards
  @CreationTimestamp
  private LocalDateTime createdAt;

  @UpdateTimestamp
  private LocalDateTime updatedAt;

  // Also add addTopic, removeTopic helper methods for later when able to edit
  // modules

  // Constructors
  protected CourseModule() {
  }

  public CourseModule(User user, String course_code, String schoolSem, List<ModuleTopic> topics) {
    this.user = user;
    this.courseCode = course_code;
    this.schoolSem = schoolSem;
    setTopics(topics);
  }

  public Long getId() {
    return id;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public String getCourseCode() {
    return courseCode;
  }

  public void setCourseCode(String courseCode) {
    this.courseCode = courseCode;
  }

  public String getSchoolSem() {
    return schoolSem;
  }

  public void setSchoolSem(String schoolSem) {
    this.schoolSem = schoolSem;
  }

  public List<ModuleTopic> getTopics() {
    return topics;
  }

  public void setTopics(List<ModuleTopic> topics) {
    // Deepcopy of the list of topics
    List<ModuleTopic> incomingTopics = new ArrayList<>(topics);
    this.topics.clear();
    for (ModuleTopic topic : incomingTopics) {
      addTopic(topic);
    }
  }

  public void addTopic(ModuleTopic topic) {
    topics.add(topic);

    // Setting on both sides so the bidirectional relationship is in sync
    topic.setCourseModule(this);
  }

  public void removeTopic(ModuleTopic topic) {
    topics.remove(topic);
    topic.setCourseModule(null);
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

}
