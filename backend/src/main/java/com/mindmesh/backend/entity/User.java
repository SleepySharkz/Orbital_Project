package com.mindmesh.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false, unique = false)
  private String username;

  @Column(nullable = false)
  private String passwordHash;

  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @PrePersist // What to do at entity creation time
  protected void onCreate() {
    this.createdAt = LocalDateTime.now();
  }

  // constructors
  protected User() { // This is to be used by JPA, NOT FOR business logic
  } // JPA requires empty constructor

  public User(String username, String email, String passwordHash) {
    this.email = email;
    this.passwordHash = passwordHash;
    this.username = username;
  }

  // getters and setters
  public Long getId() {
    return id;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public void setPasswordHash(String passwordHash) {
    this.passwordHash = passwordHash;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public String getUsername() {
    return this.username;
  }

  public void setUsername(String username) {
    this.username = username;
  }
}
