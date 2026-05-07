package com.mindmesh.backend.dto;

public class UserDto {
  private Long id;
  private String email;

  public UserDto(Long id, String email) {
    this.id = id;
    this.email = email;
  }

  public Long getId() {
    return id;
  }

  public String getEmail() {
    return email;
  }
}
