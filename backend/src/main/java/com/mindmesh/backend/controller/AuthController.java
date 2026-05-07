package com.mindmesh.backend.controller;

import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import com.mindmesh.backend.entity.User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mindmesh.backend.dto.UserDto;
import com.mindmesh.backend.dto.requests.SignupRequest;
import com.mindmesh.backend.dto.requests.LoginRequest;
import com.mindmesh.backend.dto.responses.LoginResponse;
import com.mindmesh.backend.dto.responses.MessageResponse;
import com.mindmesh.backend.dto.responses.SignupResponse;
import com.mindmesh.backend.exception.EmailAlreadyExistsException;
import com.mindmesh.backend.exception.EmailNotFoundException;
import com.mindmesh.backend.exception.InvalidCredentials;
import com.mindmesh.backend.service.AuthService;

@RestController
@RequestMapping("/auth")
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/signup") // TODO: Make custom restcontroller advice for custom bad input exception
  public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest signupRequest) {
    // This is where we connect the dto to the AuthService
    try {
      authService.userSignup(signupRequest);
      SignupResponse signupResponse = new SignupResponse("You have been registered successfully!");
      return ResponseEntity.ok(signupResponse);
    } catch (EmailAlreadyExistsException e) {
      SignupResponse signupResponse = new SignupResponse(e.getMessage());
      return ResponseEntity.status(409).body(signupResponse);
    }
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
    try {
      User user = authService.userLogin(loginRequest);
      UserDto userDto = new UserDto(user.getId(), user.getEmail());
      String token = "jwt-token"; // TODO: Implement JWT

      LoginResponse loginResponse = new LoginResponse(token, userDto);
      return ResponseEntity.ok(loginResponse);

    } catch (InvalidCredentials e) {
      MessageResponse messageResponse = new MessageResponse(e.getMessage());
      return ResponseEntity.status(401).body(messageResponse);
    }
  }
}
