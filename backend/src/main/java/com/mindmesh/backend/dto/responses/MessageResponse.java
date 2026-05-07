package com.mindmesh.backend.dto.responses;

public class MessageResponse {
  private String message;

  public MessageResponse(String message) {
    this.message = message;
  }

  public String getMessage() {
    return this.message;
  };
}
