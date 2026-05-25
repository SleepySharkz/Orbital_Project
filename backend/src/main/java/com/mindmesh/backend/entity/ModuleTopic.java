package com.mindmesh.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "module_topics", uniqueConstraints = {
    // No duplicate topic entries
    @UniqueConstraint(columnNames = { "module_id", "topic_name" })
})
public class ModuleTopic {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "module_id", nullable = false)
  private CourseModule courseModule;

  @Column(name = "topic_name", nullable = false)
  private String topicName;

  protected ModuleTopic() {
  }

  // Use this to create a new ModuleTopic
  public ModuleTopic(CourseModule courseModule, String topicName) {
    this.courseModule = courseModule;
    this.topicName = topicName;
  }

  public Long getId() {
    return id;
  }

  public CourseModule getCourseModule() {
    return courseModule;
  }

  public void setCourseModule(CourseModule courseModule) {
    this.courseModule = courseModule;
  }

  public String getTopicName() {
    return topicName;
  }

  public void setTopicName(String topicName) {
    this.topicName = topicName;
  }

}
