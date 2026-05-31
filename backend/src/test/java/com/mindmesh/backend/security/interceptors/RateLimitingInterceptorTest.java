package com.mindmesh.backend.security.interceptors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@ExtendWith(MockitoExtension.class)
class RateLimitingInterceptorTest {

  @Mock
  private RateLimiter rateLimiter;

  @InjectMocks
  private RateLimitingInterceptor interceptor;

  @Test
  void preHandle_bypassesRequestsOutsideConfiguredCfcCreateEndpoint() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/modules");
    MockHttpServletResponse response = new MockHttpServletResponse();

    boolean result = interceptor.preHandle(request, response, new Object());

    assertTrue(result);
    verify(rateLimiter, never()).getKeyFromRequest(request);
  }

  @Test
  void preHandle_returns429WhenRequestLimitExceeded() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/cfcs");
    MockHttpServletResponse response = new MockHttpServletResponse();

    when(rateLimiter.getKeyFromRequest(request)).thenReturn("user:17:POST:/api/v1/cfcs");
    when(rateLimiter.isRequestAllowed("user:17:POST:/api/v1/cfcs", 3)).thenReturn(false);

    boolean result = interceptor.preHandle(request, response, new Object());

    assertTrue(!result);
    assertEquals(429, response.getStatus());
    assertEquals("{\"error\": \"Too many requests\"}", response.getContentAsString());
  }
}
