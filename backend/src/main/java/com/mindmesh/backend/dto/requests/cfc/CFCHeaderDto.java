package com.mindmesh.backend.dto.requests.cfc;

import com.mindmesh.backend.enums.SourceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CFCHeaderDto {

  @NotNull
  private SourceType sourceType;

  @NotBlank
  private String sourceTitle;

  public CFCHeaderDto() {
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
}
