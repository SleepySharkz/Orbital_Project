package com.mindmesh.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.mindmesh.backend.security.interceptors.RateLimitingInterceptor;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

  private final RateLimitingInterceptor rateLimitingInterceptor;

  public InterceptorConfig(RateLimitingInterceptor rateLimitingInterceptor) {
    this.rateLimitingInterceptor = rateLimitingInterceptor;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(rateLimitingInterceptor)
        .addPathPatterns("/api/v1/cfcs");
  }
}
