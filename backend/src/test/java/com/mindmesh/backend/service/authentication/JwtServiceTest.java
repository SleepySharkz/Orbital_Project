package com.mindmesh.backend.service.authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.mindmesh.backend.entity.User;
import com.mindmesh.backend.security.jwt.TokenStatus;
import com.mindmesh.backend.service.JwtService;

@ExtendWith(MockitoExtension.class)
public class JwtServiceTest {

  private JwtService jwtService;

  @Mock
  private User mockUser;

  private static final String TEST_SECRET_BASE64 = Base64.getEncoder()
      .encodeToString(
          "mindmesh-jwt-unit-test-secret-key-32-bytes-minimum"
              .getBytes(StandardCharsets.UTF_8));

  private static final long VALID_EXPIRATION_MS = 3_600_000L;
  private static final long EXPIRED_EXPIRATION_MS = -1_000L;

  @BeforeEach
  void setUp() {
    jwtService = new JwtService();
    ReflectionTestUtils.setField(jwtService, "JWT_SECRET", TEST_SECRET_BASE64);

    when(mockUser.getEmail()).thenReturn("tauz@example.com");
    when(mockUser.getId()).thenReturn(1L);
  }

  @Test
  void generatedTokenIsValidAndRestoresUserIdentity() {
    ReflectionTestUtils.setField(
        jwtService,
        "JWT_EXPIRATION_MS",
        VALID_EXPIRATION_MS);
    String token = jwtService.generateJwtToken(mockUser);

    assertEquals(TokenStatus.VALID, jwtService.validateToken(token));
    assertEquals("tauz@example.com", jwtService.extractEmail(token));
  }

  @Test
  void expiredTokenIsRejected() {
    ReflectionTestUtils.setField(
        jwtService,
        "JWT_EXPIRATION_MS",
        EXPIRED_EXPIRATION_MS);
    String token = jwtService.generateJwtToken(mockUser);

    assertEquals(TokenStatus.EXPIRED, jwtService.validateToken(token));
  }
}
