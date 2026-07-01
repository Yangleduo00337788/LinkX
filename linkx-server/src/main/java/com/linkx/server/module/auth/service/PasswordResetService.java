package com.linkx.server.module.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private static final String KEY_PREFIX = "auth:pwd-reset:";
    private static final Duration TTL = Duration.ofMinutes(30);

    private final RedisTemplate<String, Object> redisTemplate;

    public String issueToken(Long userId) {
        String token = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(KEY_PREFIX + token, String.valueOf(userId), TTL);
        return token;
    }

    public Long consumeToken(String token) {
        if (!StringUtils.hasText(token)) {
            return null;
        }
        String key = KEY_PREFIX + token.trim();
        Object userId = redisTemplate.opsForValue().get(key);
        if (userId == null) {
            return null;
        }
        redisTemplate.delete(key);
        try {
            return Long.parseLong(String.valueOf(userId));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}