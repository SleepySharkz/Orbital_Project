package com.mindmesh.backend.security.interceptors;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RateLimitingInterceptor implements HandlerInterceptor {
  private static final String CFC_CREATE_PATH = "/api/v1/cfcs";

  private final RateLimiter rateLimiterService;

  public RateLimitingInterceptor(RateLimiter rateLimiterService) {
    this.rateLimiterService = rateLimiterService;
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

    if (!"POST".equalsIgnoreCase(request.getMethod())
        || !CFC_CREATE_PATH.equals(request.getRequestURI())) {
      return true;
    }

    Integer maxRequests = 3;

    String key = rateLimiterService.getKeyFromRequest(request);
    if (!rateLimiterService.isRequestAllowed(key, maxRequests)) {
      response.setHeader("Retry-After", "60"); // Wait 60 seconds
      response.setStatus(429);
      response.setContentType("application/json");
      response.getWriter().write("{\"error\": \"Too many requests\"}");
      return false;
    }

    return true;
  }

  // @Override
  // public void postHandle(
  // HttpServletRequest request,
  // HttpServletResponse response,
  // Object handler,
  // ModelAndView modelAndView) throws Exception {
  //
  // }
  //
}
