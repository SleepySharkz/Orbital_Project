package com.mindmesh.backend.service;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.mindmesh.backend.entity.User;
import com.mindmesh.backend.security.jwt.TokenStatus;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

// For now we will stick with single tokens (No refresh - access token).
// But sure later can modify
// TODO: Augment JWT to use refresh tokens
@Service
public class JwtService {

  @Value("${jwt.secret}")
  private String JWT_SECRET;

  @Value("${jwt.expiration-ms}")
  private long JWT_EXPIRATION_MS;

  public String generateJwtToken(User user) {
    return Jwts.builder()
        .subject(user.getEmail())
        .claim("userId", user.getId())
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION_MS))
        .signWith(getSignature())
        .compact();
  }

  public TokenStatus validateToken(String token) {
    try {
      extractAllClaims(token);
      return TokenStatus.VALID;
    } catch (ExpiredJwtException e) {
      return TokenStatus.EXPIRED;
    } catch (SignatureException e) {
      return TokenStatus.MANIPULATED;
    } catch (MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
      return TokenStatus.INVALIDFORMAT;
    }
  }

  public String extractEmail(String token) {
    Claims claims = extractAllClaims(token);
    // Subject here refers to the Email
    return claims.getSubject();
  }

  public Long extractId(String token) {
    Claims claims = extractAllClaims(token);
    return claims.get("userId", Long.class);
  }

  private Claims extractAllClaims(String token) {
    return Jwts
        .parser()
        .verifyWith(getSignature())
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  private SecretKey getSignature() {
    byte[] keyBytes = Decoders.BASE64.decode(JWT_SECRET);
    return Keys.hmacShaKeyFor(keyBytes);
  }
}
