package com.mindmesh.backend.service;

import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.mindmesh.backend.entity.User;
import com.mindmesh.backend.repository.UserRepository;
import com.mindmesh.backend.security.CustomUserDetails;

// Minimal Service to create userdetails object for secuirty context
@Service
public class CustomUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  public CustomUserDetailsService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException("User not found: " +
            email));

    List<? extends GrantedAuthority> authorities = List.of(); // No authorities for now

    return new CustomUserDetails(
        user.getId(),
        user.getEmail(),
        user.getPasswordHash(),
        authorities);

    // return org.springframework.security.core.userdetails.User
    // .withUsername(user.getEmail())
    // .password(user.getPasswordHash())
    // .authorities(List.of())
    // .build();
  }
}
