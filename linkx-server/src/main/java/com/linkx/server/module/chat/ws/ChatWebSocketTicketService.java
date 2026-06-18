package com.linkx.server.module.chat.ws;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class ChatWebSocketTicketService {

    private static final long TICKET_TTL_MILLIS = 60_000L;
    private static final String KEY_PREFIX = "chat:ws-ticket:";

    private final RedisTemplate<String, Object> redisTemplate;
    private final SecureRandom secureRandom = new SecureRandom();

    public String createTicket(Long userId) {
        byte[] randomBytes = new byte[24];
        secureRandom.nextBytes(randomBytes);
        String ticket = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        redisTemplate.opsForValue().set(buildKey(ticket), userId, Duration.ofMillis(TICKET_TTL_MILLIS));
        return ticket;
    }

    public Long consumeTicket(String ticket) {
        if (!StringUtils.hasText(ticket)) {
            return null;
        }
        Object cachedUserId = redisTemplate.opsForValue().getAndDelete(buildKey(ticket));
        if (cachedUserId instanceof Number number) {
            return number.longValue();
        }
        if (cachedUserId instanceof String text && StringUtils.hasText(text)) {
            try {
                return Long.parseLong(text.trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private String buildKey(String ticket) {
        return KEY_PREFIX + ticket.trim();
    }
}
