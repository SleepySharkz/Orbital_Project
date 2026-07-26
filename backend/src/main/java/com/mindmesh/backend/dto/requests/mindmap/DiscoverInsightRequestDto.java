package com.mindmesh.backend.dto.requests.mindmap;

import java.util.List;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class DiscoverInsightRequestDto {

  @NotNull(message = "TC ids are required.")
  @Size(min = 2, max = 2, message = "Exactly two TC ids are required.")
  private List<@NotNull @Positive Long> tcIds;

  public DiscoverInsightRequestDto() {
  }

  public DiscoverInsightRequestDto(List<Long> tcIds) {
    this.tcIds = tcIds;
  }

  public List<Long> getTcIds() {
    return tcIds;
  }

  public void setTcIds(List<Long> tcIds) {
    this.tcIds = tcIds;
  }
}