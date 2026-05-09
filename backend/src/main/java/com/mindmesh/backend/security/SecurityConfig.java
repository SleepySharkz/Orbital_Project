package com.mindmesh.backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.mindmesh.backend.security.jwt.JwtAuthFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final JwtAuthFilter jwtAuthFilter;

  public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
    this.jwtAuthFilter = jwtAuthFilter;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    // Disable default configurations
    // And customize some authorizations

    http
        .csrf(a -> a.disable())
        .formLogin(a -> a.disable())
        .httpBasic(basic -> basic.disable())
        .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin())) // Gotta allow h2-console frames
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(
                "/auth/signup",
                "/auth/login",
                "/error",
                "/h2-console/**")
            .permitAll() // signup and login
            // are public
            // But dont exist yet, so still cant access
            // .requestMatchers("/admin").hasRole("ADMIN") // IDK some admin endpoint later
            // on, for testing or override
            .anyRequest().authenticated())
        .addFilterBefore(jwtAuthFilter,
            UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}
