package com.mindmesh.backend.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class CustomUserDetails implements UserDetails {

  private final Long id;
  private final String email;
  private final String username;
  private final String passwordHash;
  private final List<? extends GrantedAuthority> authorities;

  public CustomUserDetails(Long id, String email, String username, String passwordHash,
      List<? extends GrantedAuthority> authorities) {
    this.id = id;
    this.email = email;
    this.username = username;
    this.passwordHash = passwordHash;
    this.authorities = authorities;
  }

  // New custom getID method for /auth/me endpoint
  public Long getId() {
    return id;
  }

  @Override
  public String getUsername() {
    return username;
  }

  public String getEmail() {
    return email;
  }

  @Override
  public String getPassword() {
    return passwordHash;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return authorities;
  }

  // Unused methods for now...
  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

}
