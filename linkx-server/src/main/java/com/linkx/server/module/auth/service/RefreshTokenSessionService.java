package com.linkx.server.module.auth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkx.server.module.auth.dto.AuthSessionDTO;
import com.linkx.server.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 多设备 refresh 会话：每设备一条 Redis 记录，JWT refresh 携带 {@code sid} claim。
 */
@Service
@RequiredArgsConstructor
public class RefreshTokenSessionService {

    private static final String TOKEN_KEY_PREFIX = "auth:refresh-token:";
    private static final String USER_SESSIONS_PREFIX = "auth:user-sessions:";
    private static final String LEGACY_KEY_PREFIX = "auth:refresh-token:";

    private final RedisTemplate<String, Object> redisTemplate;
    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;

    public String newSessionId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public void issueToken(Long userId, String sessionId, String refreshToken, String clientIp, String userAgent) {
        if (userId == null || !StringUtils.hasText(refreshToken) || !StringUtils.hasText(sessionId)) {
            return;
        }
        long ttlMillis = jwtTokenProvider.getRemainingValidityMillis(refreshToken);
        if (ttlMillis <= 0) {
            revokeSession(userId, sessionId);
            return;
        }
        SessionMeta meta = new SessionMeta(
                sessionId,
                summarizeUserAgent(userAgent),
                truncate(clientIp, 64),
                truncate(userAgent, 512),
                System.currentTimeMillis(),
                System.currentTimeMillis() + ttlMillis
        );
        try {
            String json = objectMapper.writeValueAsString(meta);
            redisTemplate.opsForValue().set(tokenKey(userId, sessionId), hashToken(refreshToken), Duration.ofMillis(ttlMillis));
            redisTemplate.opsForValue().set(metaKey(userId, sessionId), json, Duration.ofMillis(ttlMillis));
            redisTemplate.opsForSet().add(userSessionsKey(userId), sessionId);
            redisTemplate.expire(userSessionsKey(userId), ttlMillis, TimeUnit.MILLISECONDS);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("session meta serialize failed", e);
        }
    }

    public boolean matchesActiveToken(Long userId, String sessionId, String refreshToken) {
        if (userId == null || !StringUtils.hasText(refreshToken)) {
            return false;
        }
        if (!StringUtils.hasText(sessionId)) {
            return matchesLegacySingleToken(userId, refreshToken);
        }
        Object storedHash = redisTemplate.opsForValue().get(tokenKey(userId, sessionId));
        return storedHash != null && hashToken(refreshToken).equals(String.valueOf(storedHash));
    }

    public void revokeSession(Long userId, String sessionId) {
        if (userId == null || !StringUtils.hasText(sessionId)) {
            return;
        }
        redisTemplate.delete(tokenKey(userId, sessionId));
        redisTemplate.delete(metaKey(userId, sessionId));
        redisTemplate.opsForSet().remove(userSessionsKey(userId), sessionId);
    }

    /** 吊销该用户全部 refresh 会话 */
    public void revokeAllForUser(Long userId) {
        if (userId == null) {
            return;
        }
        Set<Object> ids = redisTemplate.opsForSet().members(userSessionsKey(userId));
        if (ids != null) {
            for (Object id : ids) {
                if (id != null) {
                    revokeSession(userId, String.valueOf(id));
                }
            }
        }
        redisTemplate.delete(userSessionsKey(userId));
        redisTemplate.delete(legacyKey(userId));
    }

    /** 兼容旧接口：吊销全部会话 */
    public void revokeToken(Long userId) {
        revokeAllForUser(userId);
    }

    public void revokeOtherSessions(Long userId, String keepSessionId) {
        if (userId == null) {
            return;
        }
        Set<Object> ids = redisTemplate.opsForSet().members(userSessionsKey(userId));
        if (ids == null) {
            return;
        }
        for (Object id : ids) {
            String sid = String.valueOf(id);
            if (!sid.equals(keepSessionId)) {
                revokeSession(userId, sid);
            }
        }
    }

    public List<AuthSessionDTO> listSessions(Long userId, String currentSessionId) {
        List<AuthSessionDTO> result = new ArrayList<>();
        if (userId == null) {
            return result;
        }
        Set<Object> ids = redisTemplate.opsForSet().members(userSessionsKey(userId));
        if (ids == null || ids.isEmpty()) {
            return result;
        }
        for (Object id : ids) {
            String sessionId = String.valueOf(id);
            SessionMeta meta = readMeta(userId, sessionId);
            if (meta == null) {
                continue;
            }
            result.add(AuthSessionDTO.builder()
                    .sessionId(sessionId)
                    .deviceLabel(meta.deviceLabel())
                    .clientIp(meta.clientIp())
                    .userAgent(meta.userAgent())
                    .issuedAt(toLocalDateTime(meta.issuedAtMillis()))
                    .expiresAt(toLocalDateTime(meta.expiresAtMillis()))
                    .current(sessionId.equals(currentSessionId))
                    .build());
        }
        result.sort(Comparator.comparing(AuthSessionDTO::getIssuedAt, Comparator.nullsLast(Comparator.reverseOrder())));
        return result;
    }

    private SessionMeta readMeta(Long userId, String sessionId) {
        Object raw = redisTemplate.opsForValue().get(metaKey(userId, sessionId));
        if (raw == null) {
            return null;
        }
        try {
            return objectMapper.readValue(String.valueOf(raw), SessionMeta.class);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private boolean matchesLegacySingleToken(Long userId, String refreshToken) {
        Object storedHash = redisTemplate.opsForValue().get(legacyKey(userId));
        return storedHash != null && hashToken(refreshToken).equals(String.valueOf(storedHash));
    }

    private String tokenKey(Long userId, String sessionId) {
        return TOKEN_KEY_PREFIX + userId + ":" + sessionId;
    }

    private String metaKey(Long userId, String sessionId) {
        return TOKEN_KEY_PREFIX + userId + ":" + sessionId + ":meta";
    }

    private String userSessionsKey(Long userId) {
        return USER_SESSIONS_PREFIX + userId;
    }

    private String legacyKey(Long userId) {
        return LEGACY_KEY_PREFIX + userId;
    }

    private static LocalDateTime toLocalDateTime(long millis) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault());
    }

    static String summarizeUserAgent(String userAgent) {
        if (!StringUtils.hasText(userAgent)) {
            return "未知设备";
        }
        String ua = userAgent.trim();
        if (ua.length() > 80) {
            return ua.substring(0, 77) + "...";
        }
        return ua;
    }

    private static String truncate(String value, int max) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String v = value.trim();
        return v.length() <= max ? v : v.substring(0, max);
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

    record SessionMeta(
            String sessionId,
            String deviceLabel,
            String clientIp,
            String userAgent,
            long issuedAtMillis,
            long expiresAtMillis
    ) {
    }
}