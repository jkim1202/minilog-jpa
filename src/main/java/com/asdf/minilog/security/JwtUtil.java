package com.asdf.minilog.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.io.Serializable;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil implements Serializable {
  private static final long serialVersionUID = -2550185165626007488L;
  public static final long JWT_VALIDITY = 5 * 60 * 60;

  @Value("${jwt.secret}")
  private String secret;

  public String getUsernameFromToken(String token) {
    return getClaimFromToken(token, Claims::getSubject);
  }

  public Long getUserIdFromToken(String token) {
    String jwt;
    if (token.startsWith("Bearer ")) {
      jwt = token.substring(7); // "Bearer " 접두사 제거
    } else {
      jwt = token; // "Bearer " 접두사 없는 경우
    }
    return getClaimFromToken(jwt, claims -> claims.get("userId", Long.class));
  }

  private <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
    Claims claims = getAllClaimsFromToken(token);
    return claimsResolver.apply(claims);
  }

  private Claims getAllClaimsFromToken(String token) {
    Key signingKey =
        new SecretKeySpec(
            Base64.getDecoder().decode(secret), SignatureAlgorithm.HS256.getJcaName());
    return Jwts.parserBuilder().setSigningKey(signingKey).build().parseClaimsJws(token).getBody();
  }

  private Boolean isTokenExpired(String token) {
    Date expiration = getExpirationDateFromToken(token);
    return expiration.before(new Date());
  }

  private Date getExpirationDateFromToken(String token) {
    return getClaimFromToken(token, Claims::getExpiration);
  }

  public String generateToken(UserDetails userDetails, Long userId) {
    // Claims는 JWT의 payload에 저장되는 정보를 담는 객체
    Map<String, Object> claims = new HashMap<>();
    claims.put("userId", userId);

    // JWT 생성
    return Jwts.builder()
        .setClaims(claims)
        .setSubject(userDetails.getUsername())
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(new Date(System.currentTimeMillis() + JWT_VALIDITY * 1000))
        .signWith(
            new SecretKeySpec(
                Base64.getDecoder().decode(secret), SignatureAlgorithm.HS512.getJcaName()))
        .compact();
  }

  public Boolean validateToken(String token, UserDetails userDetails) {
    String username = getUsernameFromToken(token);
    return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
  }
}
