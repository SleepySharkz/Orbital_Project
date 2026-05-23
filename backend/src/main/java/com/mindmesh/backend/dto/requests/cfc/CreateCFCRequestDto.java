package com.mindmesh.backend.dto.requests.cfc;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class CreateCFCRequestDto {
  @NotNull
  private Long moduleId;

  @Valid
  @NotNull
  private CFCHeaderDto flashcardHeader;

  @Valid
  @NotEmpty
  private List<QnNotePairDto> items;

  public CreateCFCRequestDto() {
  }

  public Long getModuleId() {
    return moduleId;
  }

  public void setModuleId(Long moduleId) {
    this.moduleId = moduleId;
  }

  public CFCHeaderDto getFlashcardHeader() {
    return flashcardHeader;
  }

  public void setFlashcardHeader(CFCHeaderDto header) {
    this.flashcardHeader = header;
  }

  public List<QnNotePairDto> getItems() {
    return items;
  }

  public void setItems(List<QnNotePairDto> items) {
    this.items = items;
  }
}
