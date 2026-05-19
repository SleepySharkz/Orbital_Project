package com.mindmesh.backend.dto.requests;

import jakarta.validation.constraints.*;

public class SignupRequest {

  @NotBlank(message = "email is required")
  @Email(message = "Invalid email format") // Easy email formatting check
  private String email;

  @NotBlank(message = "password is required")
  @Size(min = 8, message = "Password must be at least 8 characters")
  private String password; // Note that this is the original non hashed password
                           // We only hash password after it has reached the server, for security reasons

  @NotBlank(message = "Username required")
  private String username;

  public SignupRequest() {
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

  public String getUsername() {
    return this.username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

}
