package com.mindmesh.backend.dto.responses;

public class AuthResponse {

  private String username;

  private String email;

  public AuthResponse(String username, String email) {
    this.username = username;
    this.email = email;
  }

  public String getUsername() {
    return this.username;
  }

  public String getEmail() {
    return this.email;
  }
}
