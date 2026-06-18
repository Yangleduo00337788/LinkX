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

@Service
@RequiredArgsConstructor
public class FileAccessTicketService {

    private static final long TICKET_TTL_MILLIS = 10 * 60_000L;
    private static final String KEY_PREFIX = "file:access-ticket:";

    private final SysFileMapper fileMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final SecureRandom secureRandom = new SecureRandom();

    public String createTicket(Long fileId) {
        byte[] randomBytes = new byte[24];
        secureRandom.nextBytes(randomBytes);
        String ticket = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        redisTemplate.opsForValue().set(buildKey(ticket), fileId, Duration.ofMillis(TICKET_TTL_MILLIS));
        return ticket;
    }

    public SysFile resolveFile(String ticket) {
        if (!StringUtils.hasText(ticket)) {
            return null;
        }
        Object cachedFileId = redisTemplate.opsForValue().get(buildKey(ticket));
        if (!(cachedFileId instanceof Number fileId)) {
            return null;
        }
        return fileMapper.selectById(fileId.longValue());
    }

    private String buildKey(String ticket) {
        return KEY_PREFIX + ticket.trim();
    }
}
