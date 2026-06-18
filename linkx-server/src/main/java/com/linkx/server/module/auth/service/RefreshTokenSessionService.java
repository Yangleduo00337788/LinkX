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

@Service
@RequiredArgsConstructor
public class RefreshTokenSessionService {

    private static final String KEY_PREFIX = "auth:refresh-token:";

    private final RedisTemplate<String, Object> redisTemplate;
    private final JwtTokenProvider jwtTokenProvider;

    public void issueToken(Long userId, String refreshToken) {
        if (userId == null || !StringUtils.hasText(refreshToken)) {
            return;
        }
        long ttlMillis = jwtTokenProvider.getRemainingValidityMillis(refreshToken);
        if (ttlMillis <= 0) {
            revokeToken(userId);
            return;
        }
        redisTemplate.opsForValue().set(buildKey(userId), hashToken(refreshToken), Duration.ofMillis(ttlMillis));
    }

    public boolean matchesActiveToken(Long userId, String refreshToken) {
        if (userId == null || !StringUtils.hasText(refreshToken)) {
            return false;
        }
        String key = buildKey(userId);
        Object storedHash = redisTemplate.opsForValue().get(key);
        return storedHash != null && hashToken(refreshToken).equals(String.valueOf(storedHash));
    }

    public void revokeToken(Long userId) {
        if (userId == null) {
            return;
        }
        redisTemplate.delete(buildKey(userId));
    }

    private String buildKey(Long userId) {
        return KEY_PREFIX + userId;
    }

    private String hashToken(String refreshToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encoded = digest.digest(refreshToken.trim().getBytes(StandardCharsets.UTF_8));
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
