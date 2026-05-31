package com.mindmesh.backend.security.interceptors;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.mindmesh.backend.security.CustomUserDetails;

import jakarta.servlet.http.HttpServletRequest;

@Primary
@Service
public class SlidingWindowRateLimiterService implements RateLimiter {

  // ConcurrentHashMap is thread-safe so safe for concurrent requests
  private final ConcurrentHashMap<String, ConcurrentLinkedDeque<Instant>> requestLog = new ConcurrentHashMap<>();

  @Override
  public boolean isRequestAllowed(String userIDAndEndpointKey, Integer maxRequests) {
    Instant timeNow = Instant.now();

    // atomic, no race condition between check and insert
    ConcurrentLinkedDeque<Instant> timeWindow = requestLog.computeIfAbsent(
        userIDAndEndpointKey, k -> new ConcurrentLinkedDeque<>());

    // ALGO LOGIC HERE — but this block still needs synchronization
    synchronized (timeWindow) {
      // clean up old entries outside the window
      Instant windowStart = timeNow.minusSeconds(60); // time window is set at ~1 min
      while (!timeWindow.isEmpty() && timeWindow.peekFirst().isBefore(windowStart)) {
        timeWindow.pollFirst();
      }

      // This is not O(1) due to synchronization primitives inbuilt
      if (timeWindow.size() >= maxRequests) {
        return false;
      }

      timeWindow.addLast(timeNow);
      return true;
    }
  }

  @Override
  public String getKeyFromRequest(HttpServletRequest request) {

    // INFO: Currently it only ratelimits /api/v1/cfcs endpoint, as it contains
    // heavy AI processing
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication != null
        && authentication.isAuthenticated()
        && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
      return "user:" + userDetails.getId()
          + ":" + request.getMethod().toUpperCase()
          + ":" + request.getRequestURI();
    }

    return "anonymous:"
        + request.getMethod().toUpperCase()
        + ":" + request.getRequestURI();
  }
}
