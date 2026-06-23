package com.linkx.server.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 签发与解析（HS256）。
 * <p>
 * access 与 refresh 通过 claim {@code type} 区分；subject 为 userId 字符串。
 * 启动时校验密钥非空、非占位、长度 ≥32。
 * </p>
 */
@Slf4j
@Component
public class JwtTokenProvider {

    /** access 令牌类型，用于 API 鉴权 */
    public static final String TOKEN_TYPE_ACCESS = "access";
    /** refresh 令牌类型，仅用于刷新 access */
    public static final String TOKEN_TYPE_REFRESH = "refresh";
    private static final String DEFAULT_JWT_SECRET_PLACEHOLDER = "PLEASE_CHANGE_ME_LINKX_JWT_SECRET_2026_32CHARS_MIN";
    private static final int MIN_SECRET_LENGTH = 32;

    @Value("${linkx.jwt.secret}")
    private String jwtSecret;

    @Value("${linkx.jwt.expiration}")
    private long jwtExpiration;

    @Value("${linkx.jwt.refresh-expiration}")
    private long refreshExpiration;

    /** 防止误用默认密钥上线 */
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

    /**
     * 签发 access token。
     *
     * @param userId   用户主键
     * @param username 登录名（写入 claim，便于日志与展示）
     */
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

    /** 签发 refresh token（不含 username claim） */
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

    /**
     * 解析令牌；过期时仍返回 claims 并标记 expired=true，便于刷新流程判断。
     *
     * @return 无效令牌返回 null
     */
    public ParsedToken parseToken(String token) {
        if (!StringUtils.hasText(token)) {
            return null;
        }
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token.trim())
                    .getPayload();
            return new ParsedToken(claims);
        } catch (ExpiredJwtException exception) {
            return new ParsedToken(exception.getClaims(), true);
        } catch (JwtException | IllegalArgumentException exception) {
            log.debug("JWT parse failed: {}", exception.getMessage());
            return null;
        }
    }

    public Long getUserIdFromToken(String token) {
        ParsedToken parsed = parseToken(token);
        if (parsed == null || parsed.claims() == null) {
            throw new JwtException("Invalid token");
        }
        return Long.parseLong(parsed.claims().getSubject());
    }

    public String getUsernameFromToken(String token) {
        ParsedToken parsed = requireParsed(token);
        return parsed.claims().get("username", String.class);
    }

    public String getTokenType(String token) {
        ParsedToken parsed = requireParsed(token);
        return parsed.claims().get("type", String.class);
    }

    public boolean isAccessToken(String token) {
        ParsedToken parsed = parseToken(token);
        if (parsed == null || parsed.claims() == null) {
            return false;
        }
        String tokenType = parsed.claims().get("type", String.class);
        return TOKEN_TYPE_ACCESS.equals(tokenType);
    }

    public boolean isRefreshToken(String token) {
        ParsedToken parsed = parseToken(token);
        if (parsed == null || parsed.claims() == null) {
            return false;
        }
        return TOKEN_TYPE_REFRESH.equals(parsed.claims().get("type", String.class));
    }

    /** 签名有效且未过期 */
    public boolean validateToken(String token) {
        ParsedToken parsed = parseToken(token);
        return parsed != null && parsed.claims() != null && !parsed.expired();
    }

    public boolean isTokenExpired(String token) {
        ParsedToken parsed = parseToken(token);
        return parsed == null || parsed.expired();
    }

    /** 用于 access 黑名单 TTL：与令牌剩余有效期对齐 */
    public long getRemainingValidityMillis(String token) {
        ParsedToken parsed = requireParsed(token);
        long remaining = parsed.claims().getExpiration().getTime() - System.currentTimeMillis();
        return Math.max(remaining, 0L);
    }

    private ParsedToken requireParsed(String token) {
        ParsedToken parsed = parseToken(token);
        if (parsed == null || parsed.claims() == null) {
            throw new JwtException("Invalid token");
        }
        return parsed;
    }

    /**
     * @param claims  JWT 载荷
     * @param expired 是否已过期（解析时捕获 ExpiredJwtException 则为 true）
     */
    public record ParsedToken(Claims claims, boolean expired) {
        ParsedToken(Claims claims) {
            this(claims, claims.getExpiration().before(new Date()));
        }
    }
}