package com.mindmesh.backend.dto.requests.ModuleRelated;

import jakarta.validation.constraints.NotBlank;

public class ModuleTopicDto {
  @NotBlank(message = "Topic must be non empty")
  private String topic;

  public ModuleTopicDto() {
  }

  public void setTopic(String topic) {
    this.topic = topic;
  }

  public String getTopic() {
    return this.topic;
  }
}
