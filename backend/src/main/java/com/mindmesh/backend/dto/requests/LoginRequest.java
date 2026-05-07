package com.mindmesh.backend.dto.requests;

import jakarta.validation.constraints.*;

public class LoginRequest {

  @NotBlank(message = "email is required")
  @Email(message = "Invalid email format") // Easy email formatting check
  private String email;

  @NotBlank(message = "password is required")
  @Size(min = 8, message = "Password must be at least 8 characters") // Just a thing
  private String password; // Note that this is the original non hashed password
                           // We only hash password after it has reached the server, for security reasons

  public LoginRequest() {
  }

  public String getEmail() {
    return this.email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return this.password;
  }

  public void setPassword(String password) {
    this.password = password;
  }
}
