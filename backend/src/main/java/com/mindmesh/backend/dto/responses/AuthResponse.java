package com.mindmesh.backend.dto.responses;

public class AuthResponse {

  private Long id;

  private String email;

  public AuthResponse(Long id, String email) {
    this.id = id;
    this.email = email;
  }

  public Long getId() {
    return this.id;
  }

  public String getEmail() {
    return this.email;
  }
}
