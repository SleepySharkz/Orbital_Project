package com.mindmesh.backend.dto.responses;

import com.mindmesh.backend.dto.UserDto;

public class LoginResponse {
  private String token;
  private UserDto userDto; // Trimmed down user info (Only what client needs)

  public LoginResponse(String token, UserDto userDto) {
    this.token = token;
    this.userDto = userDto;
  }

  public String getToken() {
    return token;
  }

  public UserDto getUser() {
    return userDto;
  }
}
