package com.mindmesh.backend.dto.requests.cfc;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class QnNotePairDto {
  @NotNull
  private Long itemId;

  @NotBlank
  private String topic;

  // NO validation here, the either-or check is done at service-level
  private String questionText;

  private List<@NotBlank String> imageKeys;

  @NotBlank
  private String roughNote;

  public Long getItemId() {
    return itemId;
  }

  public void setItemId(Long itemId) {
    this.itemId = itemId;
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

  public List<String> getImageKeys() {
    return imageKeys;
  }

  public void setImageKeys(List<String> imageKeys) {
    this.imageKeys = imageKeys;
  }

  public String getRoughNote() {
    return roughNote;
  }

  public void setRoughNote(String roughNote) {
    this.roughNote = roughNote;
  }
}
