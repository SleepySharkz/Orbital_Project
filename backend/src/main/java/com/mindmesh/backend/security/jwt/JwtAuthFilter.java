package com.mindmesh.backend.security.jwt;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.mindmesh.backend.service.CustomUserDetailsService;
import com.mindmesh.backend.service.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final CustomUserDetailsService userDetailsService;

  public JwtAuthFilter(JwtService service, CustomUserDetailsService uService) {
    this.jwtService = service;
    this.userDetailsService = uService;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    // Extract token from the request
    String authHeader = request.getHeader("Authorization");

    // Ignore other auth schemes that use the same Authorization header
    // Authorization: Basic dXNlcjpwYXNz... <- Basic auth (username:password)
    // Authorization: Bearer eyJhbG... <- Bearer token (JWT)
    // Authorization: ApiKey abc123... <- API key
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }
    
    //first 7 chars are Bearer, token is after that
    String token = authHeader.substring(7);

    // validateToken
    // We allow other uncaught runtime exceptions to bubble up as part of our design
    TokenStatus tokenStatus = jwtService.validateToken(token);

    if (tokenStatus == TokenStatus.VALID
        && SecurityContextHolder.getContext().getAuthentication() == null) {
      // Null check so as to not overwrite previous already authenticated requests

      String userEmail = jwtService.extractEmail(token);
      UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

      // Now need to tell spring that this fella's request is authenticated

      // create a Spring authentication interface object with the user's identity
      UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
          userDetails, // Pass in userDetails object, as it carries more info, and for future role
                       // assignment
          null, // Null credentials, since already handled by jwt
          userDetails.getAuthorities());

      // store it in the SecurityContext for this request
      // And this filter class happens interjects before the default
      // UsernamePasswordAuthentication filter, just so that we can authenticate user
      // with JWT
      SecurityContextHolder.getContext().setAuthentication(authentication);
    } else if (tokenStatus == TokenStatus.MANIPULATED) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.setContentType("application/json");
      response.getWriter().write("{\"message\":\"Invalid token signature\"}");
      return;
    } else if (tokenStatus == TokenStatus.EXPIRED) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.setContentType("application/json");
      response.getWriter().write("{\"message\":\"Token has expired\"}");
      return;
    } else if (tokenStatus == TokenStatus.INVALIDFORMAT) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.setContentType("application/json");
      response.getWriter().write("{\"message\":\"Malformed or unsupported token\"}");
      return;
    }

    // resume filter chain
    filterChain.doFilter(request, response);
  }
}
