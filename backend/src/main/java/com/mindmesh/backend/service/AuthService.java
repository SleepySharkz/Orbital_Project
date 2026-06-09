package com.mindmesh.backend.service;

import java.util.Locale;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.mindmesh.backend.dto.requests.LoginRequest;
import com.mindmesh.backend.dto.requests.SignupRequest;
import com.mindmesh.backend.entity.User;
import com.mindmesh.backend.exception.EmailAlreadyExistsException;
import com.mindmesh.backend.exception.InvalidCredentials;
import com.mindmesh.backend.repository.UserRepository;

@Service
public class AuthService {

  private final PasswordEncoder passwordEncoder;
  private final UserRepository userRepo; // We will avoid autoinjection by spring, as recommended by spring, to avoid
                                         // reassigment, and aid testing

  public AuthService(UserRepository userRepo, PasswordEncoder passwordEncoder) {
    this.userRepo = userRepo;
    this.passwordEncoder = passwordEncoder;
  }

  public void getUserFromSignup(SignupRequest signupRequest) { // No need to declar throws since its unchecked
    String psw = signupRequest.getPassword();
    String username = signupRequest.getUsername();
    String pswHash = passwordEncoder.encode(psw);
    String signUpEmail = signupRequest.getEmail();

    if (userRepo.findByEmailIgnoreCase(signUpEmail).isPresent()) {
      throw new EmailAlreadyExistsException(signUpEmail);
    }

    // Create User
    User user = new User(username, signUpEmail, pswHash);
    userRepo.save(user);
  }

  public User getUserFromLogin(LoginRequest loginRequest) {
    String loginEmail = loginRequest.getEmail();
    loginEmail = loginEmail.trim().toLowerCase(Locale.ROOT);
    String loginPsw = loginRequest.getPassword();

    User user = userRepo
        .findByEmailIgnoreCase(loginEmail) // returns an Optional<User>
        .orElseThrow(() -> new InvalidCredentials());

    String trueHash = user.getPasswordHash();

    Boolean isMatch = passwordEncoder.matches(loginPsw, trueHash);

    if (!isMatch) {
      throw new InvalidCredentials();
    }

    return user;
  }

  public void logoutUser() {
    // Super simple for now
    // /logout is protected so no need to worry about authentication
    SecurityContextHolder.clearContext();
  }
}
