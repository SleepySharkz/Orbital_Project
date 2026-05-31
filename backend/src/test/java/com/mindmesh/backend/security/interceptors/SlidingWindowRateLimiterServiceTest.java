package com.mindmesh.backend.security.interceptors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import com.mindmesh.backend.security.CustomUserDetails;

class SlidingWindowRateLimiterServiceTest {

  private final SlidingWindowRateLimiterService rateLimiterService = new SlidingWindowRateLimiterService();

  @AfterEach
  void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void getKeyFromRequest_returnsUserMethodAndPath_forAuthenticatedUser() {
    CustomUserDetails userDetails = new CustomUserDetails(
        17L,
        "tauzih@example.com",
        "Tauzih",
        "hashed",
        AuthorityUtils.NO_AUTHORITIES);

    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));

    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/cfcs");

    String key = rateLimiterService.getKeyFromRequest(request);

    assertEquals("user:17:POST:/api/v1/cfcs", key);
  }

  @Test
  void getKeyFromRequest_returnsAnonymousKey_whenNoAuthenticatedUser() {
    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/cfcs");

    String key = rateLimiterService.getKeyFromRequest(request);

    assertEquals("anonymous:POST:/api/v1/cfcs", key);
  }

  @Test
  void isRequestAllowed_rejectsRequestWhenLimitExceededWithinWindow() {
    String key = "user:17:POST:/api/v1/cfcs";

    assertTrue(rateLimiterService.isRequestAllowed(key, 2));
    assertTrue(rateLimiterService.isRequestAllowed(key, 2));
    assertFalse(rateLimiterService.isRequestAllowed(key, 2));
  }

  @Test
  void isRequestAllowed_prunesExpiredEntriesBeforeApplyingLimit() {
    String key = "user:17:POST:/api/v1/cfcs";

    @SuppressWarnings("unchecked")
    ConcurrentHashMap<String, ConcurrentLinkedDeque<Instant>> requestLog =
        (ConcurrentHashMap<String, ConcurrentLinkedDeque<Instant>>) ReflectionTestUtils.getField(
            rateLimiterService,
            "requestLog");

    ConcurrentLinkedDeque<Instant> timestamps = new ConcurrentLinkedDeque<>();
    timestamps.add(Instant.now().minusSeconds(61));
    requestLog.put(key, timestamps);

    assertTrue(rateLimiterService.isRequestAllowed(key, 1));
  }
}
