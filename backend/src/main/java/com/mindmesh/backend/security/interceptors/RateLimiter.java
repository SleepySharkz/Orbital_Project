package com.mindmesh.backend.security.interceptors;

import jakarta.servlet.http.HttpServletRequest;

public interface RateLimiter {
  boolean isRequestAllowed(String requestKey, Integer maxRequests);

  String getKeyFromRequest(HttpServletRequest request);

}
