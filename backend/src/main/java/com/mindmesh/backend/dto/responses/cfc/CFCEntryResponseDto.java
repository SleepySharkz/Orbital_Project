package com.mindmesh.backend.dto.responses.cfc;

public class CFCEntryResponseDto {

  private Long id;
  private Long requestItemId;
  private String topic;
  private CFCContentDto content;
  private SourceMaterialDto sourceMaterial;

  public CFCEntryResponseDto(
      Long id,
      Long requestItemId,
      String topic,
      CFCContentDto content,
      SourceMaterialDto sourceMaterial) {
    this.id = id;
    this.requestItemId = requestItemId;
    this.topic = topic;
    this.content = content;
    this.sourceMaterial = sourceMaterial;
  }

  public Long getId() {
    return id;
  }

  public Long getRequestItemId() {
    return requestItemId;
  }

  public String getTopic() {
    return topic;
  }

  public CFCContentDto getContent() {
    return content;
  }

  public SourceMaterialDto getSourceMaterial() {
    return sourceMaterial;
  }
}
