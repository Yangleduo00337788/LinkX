package com.linkx.server.module.file.service;

import com.linkx.server.entity.SysFile;
import com.linkx.server.mapper.SysFileMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;

/**
 * 文件访问一次性 ticket（Redis，默认 10 分钟）：校验 userId 后消费并返回 {@link SysFile}。
 */
@Service
@RequiredArgsConstructor
public class FileAccessTicketService {

    private static final long TICKET_TTL_MILLIS = 10 * 60_000L;
    private static final String KEY_PREFIX = "file:access-ticket:";

    private final SysFileMapper fileMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final SecureRandom secureRandom = new SecureRandom();

    public String createTicket(Long userId, Long fileId) {
        if (userId == null || fileId == null) {
            throw new IllegalArgumentException("userId and fileId are required");
        }
        byte[] randomBytes = new byte[24];
        secureRandom.nextBytes(randomBytes);
        String ticket = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        String payload = userId + ":" + fileId;
        redisTemplate.opsForValue().set(buildKey(ticket), payload, Duration.ofMillis(TICKET_TTL_MILLIS));
        return ticket;
    }

    /**
     * Validates ticket, checks requester matches ticket owner, loads file, then consumes ticket (single use).
     */
    public SysFile consumeFile(String ticket, Long requestUserId) {
        if (requestUserId == null) {
            return null;
        }
        if (!StringUtils.hasText(ticket)) {
            return null;
        }
        String redisKey = buildKey(ticket.trim());
        Object cached = redisTemplate.opsForValue().get(redisKey);
        if (!(cached instanceof String payload) || !StringUtils.hasText(payload)) {
            return null;
        }
        int separator = payload.indexOf(':');
        if (separator <= 0 || separator >= payload.length() - 1) {
            redisTemplate.delete(redisKey);
            return null;
        }
        try {
            long ticketUserId = Long.parseLong(payload.substring(0, separator));
            long fileId = Long.parseLong(payload.substring(separator + 1));
            if (ticketUserId != requestUserId) {
                return null;
            }
            SysFile sysFile = fileMapper.selectById(fileId);
            if (sysFile == null) {
                redisTemplate.delete(redisKey);
                return null;
            }
            redisTemplate.delete(redisKey);
            return sysFile;
        } catch (NumberFormatException exception) {
            redisTemplate.delete(redisKey);
            return null;
        }
    }

    private String buildKey(String ticket) {
        return KEY_PREFIX + ticket.trim();
    }
}