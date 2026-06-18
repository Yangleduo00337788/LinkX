package com.linkx.server.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {

    public static final String TOKEN_TYPE_ACCESS = "access";
    public static final String TOKEN_TYPE_REFRESH = "refresh";
    private static final String DEFAULT_JWT_SECRET_PLACEHOLDER = "PLEASE_CHANGE_ME_LINKX_JWT_SECRET_2026_32CHARS_MIN";
    private static final int MIN_SECRET_LENGTH = 32;

    @Value("${linkx.jwt.secret}")
    private String jwtSecret;

    @Value("${linkx.jwt.expiration}")
    private long jwtExpiration;

    @Value("${linkx.jwt.refresh-expiration}")
    private long refreshExpiration;

    @PostConstruct
    public void validateConfiguration() {
        if (!StringUtils.hasText(jwtSecret)) {
            throw new IllegalStateException("linkx.jwt.secret 未配置");
        }
        String normalizedSecret = jwtSecret.trim();
        if (DEFAULT_JWT_SECRET_PLACEHOLDER.equals(normalizedSecret)) {
            throw new IllegalStateException("linkx.jwt.secret 仍在使用默认占位值，请通过环境变量配置生产密钥");
        }
        if (normalizedSecret.length() < MIN_SECRET_LENGTH) {
            throw new IllegalStateException("linkx.jwt.secret 长度不能少于 " + MIN_SECRET_LENGTH + " 个字符");
        }
        jwtSecret = normalizedSecret;
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Long userId, String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("type", TOKEN_TYPE_ACCESS)
                .claim("username", username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public String generateRefreshToken(Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshExpiration);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("type", TOKEN_TYPE_REFRESH)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return Long.parseLong(claims.getSubject());
    }

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.get("username", String.class);
    }

    public String getTokenType(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.get("type", String.class);
    }

    public boolean isAccessToken(String token) {
        String tokenType = getTokenType(token);
        // Allow legacy access tokens issued before the token type claim was introduced.
        return !StringUtils.hasText(tokenType) || TOKEN_TYPE_ACCESS.equals(tokenType);
    }

    public boolean isRefreshToken(String token) {
        return TOKEN_TYPE_REFRESH.equals(getTokenType(token));
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("JWT token validation failed", e);
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    public long getRemainingValidityMillis(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        long remaining = claims.getExpiration().getTime() - System.currentTimeMillis();
        return Math.max(remaining, 0L);
    }
}
