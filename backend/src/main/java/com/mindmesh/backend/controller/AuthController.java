package com.mindmesh.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mindmesh.backend.dto.UserDto;
import com.mindmesh.backend.dto.requests.LoginRequest;
import com.mindmesh.backend.dto.requests.SignupRequest;
import com.mindmesh.backend.dto.responses.AuthResponse;
import com.mindmesh.backend.dto.responses.LoginResponse;
import com.mindmesh.backend.dto.responses.MessageResponse;
import com.mindmesh.backend.dto.responses.SignupResponse;
import com.mindmesh.backend.entity.User;
import com.mindmesh.backend.exception.EmailAlreadyExistsException;
import com.mindmesh.backend.exception.InvalidCredentials;
import com.mindmesh.backend.security.CustomUserDetails;
import com.mindmesh.backend.service.AuthService;
import com.mindmesh.backend.service.JwtService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

  private final AuthService authService;
  private final JwtService jwtService;

  public AuthController(AuthService authService, JwtService jwtService) {
    this.authService = authService;
    this.jwtService = jwtService;
  }

  @PostMapping("/signup") // TODO: Make custom restcontroller advice for custom bad input exception
  public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest signupRequest) {
    // This is where we connect the dto to the AuthService
    try {
      authService.getUserFromSignup(signupRequest);
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
      User user = authService.getUserFromLogin(loginRequest);
      UserDto userDto = new UserDto(user.getId(), user.getEmail());
      String token = jwtService.generateJwtToken(user);

      LoginResponse loginResponse = new LoginResponse(token, userDto);
      return ResponseEntity.ok(loginResponse);

    } catch (InvalidCredentials e) {
      MessageResponse messageResponse = new MessageResponse(e.getMessage());
      return ResponseEntity.status(401).body(messageResponse);
    }
  }

  @PostMapping("/logout")
  public ResponseEntity<?> logout() {
    authService.logoutUser();
    return ResponseEntity.ok(new MessageResponse("User Logged out successfully!"));
  }

  @GetMapping("/me")
  public ResponseEntity<?> me() {
    // Since our filter blocks unauthenticated requests for /auth/me, they wont
    // touch the controller
    CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder
        .getContext()
        .getAuthentication()
        .getPrincipal();

    Long myId = userDetails.getId();
    String myEmail = userDetails.getUsername();

    AuthResponse authResponse = new AuthResponse(myId, myEmail);
    return ResponseEntity.ok(authResponse);
  }
}
