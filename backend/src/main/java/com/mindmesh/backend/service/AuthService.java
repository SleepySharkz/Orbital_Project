package com.mindmesh.backend.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.mindmesh.backend.dto.requests.LoginRequest;
import com.mindmesh.backend.dto.requests.SignupRequest;
import com.mindmesh.backend.entity.User;
import com.mindmesh.backend.exception.EmailAlreadyExistsException;
import com.mindmesh.backend.exception.EmailNotFoundException;
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

  public void userSignup(SignupRequest signupRequest) { // No need to declar throws since its unchecked
    String psw = signupRequest.getPassword();
    String pswHash = passwordEncoder.encode(psw);
    String signUpEmail = signupRequest.getEmail();

    if (userRepo.findByEmail(signUpEmail).isPresent()) {
      throw new EmailAlreadyExistsException(signUpEmail);
    }

    // Create User
    User user = new User(signUpEmail, pswHash);
    userRepo.save(user);
  }

  public User userLogin(LoginRequest loginRequest) {
    String loginEmail = loginRequest.getEmail();
    String loginPsw = loginRequest.getPassword();

    User user = userRepo
        .findByEmail(loginEmail) // returns an Optional<User>
        .orElseThrow(() -> new InvalidCredentials());

    String trueHash = user.getPasswordHash();

    Boolean isMatch = passwordEncoder.matches(loginPsw, trueHash);

    if (!isMatch) {
      throw new InvalidCredentials();
    }

    return user;
  }
}
