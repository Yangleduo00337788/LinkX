package com.linkx.server.module.auth.service;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

/**
 * 管理员强制下线：记录用户 access token 最早有效 iat，早于该时间的 JWT 一律拒绝。
 */
@Service
@RequiredArgsConstructor
public class UserSessionInvalidationService {

    private static final String KEY_PREFIX = "auth:user:session-min-iat:";
    private static final Duration KEY_TTL = Duration.ofDays(8);

    private final RedisTemplate<String, Object> redisTemplate;

    public void invalidateAllSessions(Long userId) {
        if (userId == null) {
            return;
        }
        long nowEpoch = Instant.now().getEpochSecond();
        redisTemplate.opsForValue().set(buildKey(userId), String.valueOf(nowEpoch), KEY_TTL);
    }

    public boolean isAccessTokenRevokedByKick(Long userId, Claims claims) {
        if (userId == null || claims == null) {
            return false;
        }
        Object raw = redisTemplate.opsForValue().get(buildKey(userId));
        if (raw == null) {
            return false;
        }
        long minIat;
        try {
            minIat = Long.parseLong(String.valueOf(raw));
        } catch (NumberFormatException e) {
            return false;
        }
        Date issuedAt = claims.getIssuedAt();
        if (issuedAt == null) {
            return true;
        }
        return issuedAt.toInstant().getEpochSecond() < minIat;
    }

    private String buildKey(Long userId) {
        return KEY_PREFIX + userId;
    }
}