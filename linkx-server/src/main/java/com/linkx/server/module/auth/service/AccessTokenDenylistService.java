package com.linkx.server.module.auth.service;

import com.linkx.server.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;

/**
 * 登出后的 access token 黑名单（Redis）。
 * <p>
 * Key 为 token 的 SHA-256 摘要，TTL 与令牌剩余有效期一致，避免 Redis 长期堆积。
 * </p>
 */
@Service
@RequiredArgsConstructor
public class AccessTokenDenylistService {

    private static final String KEY_PREFIX = "auth:access-deny:";

    private final RedisTemplate<String, Object> redisTemplate;
    private final JwtTokenProvider jwtTokenProvider;

    /** 将 access 加入黑名单直至其自然过期 */
    public void denyToken(String accessToken) {
        if (!StringUtils.hasText(accessToken)) {
            return;
        }
        long ttlMillis = jwtTokenProvider.getRemainingValidityMillis(accessToken.trim());
        if (ttlMillis <= 0) {
            return;
        }
        redisTemplate.opsForValue().set(buildKey(accessToken), "1", Duration.ofMillis(ttlMillis));
    }

    public boolean isDenied(String accessToken) {
        if (!StringUtils.hasText(accessToken)) {
            return false;
        }
        return Boolean.TRUE.equals(redisTemplate.hasKey(buildKey(accessToken)));
    }

    private String buildKey(String accessToken) {
        return KEY_PREFIX + hashToken(accessToken);
    }

    private String hashToken(String accessToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encoded = digest.digest(accessToken.trim().getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(encoded.length * 2);
            for (byte value : encoded) {
                builder.append(String.format("%02x", value));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm unavailable", exception);
        }
    }
}