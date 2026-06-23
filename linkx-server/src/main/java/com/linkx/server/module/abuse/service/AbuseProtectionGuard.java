package com.linkx.server.module.abuse.service;

import com.linkx.server.common.BusinessException;
import com.linkx.server.common.ErrorCode;
import com.linkx.server.config.LinkxSecurityProperties;
import com.linkx.server.module.auth.service.AuthSecurityGuard;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 公网滥用防护：用户搜索、发送好友申请的频率与每日上限。
 * <p>
 * 计数存储在 Redis；配置项见 {@link LinkxSecurityProperties#getAbuseProtection()}。
 * 超限抛出 {@link BusinessException}，错误码 {@link ErrorCode#TOO_MANY_REQUESTS}。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AbuseProtectionGuard {

    /** Redis 键前缀：按用户 ID 的搜索次数（滑动窗口） */
    private static final String SEARCH_USER_PREFIX = "abuse:search:user:";
    /** Redis 键前缀：按客户端 IP 的搜索次数 */
    private static final String SEARCH_IP_PREFIX = "abuse:search:ip:";
    /** Redis 键前缀：按用户 ID 的每日好友申请次数 */
    private static final String FRIEND_USER_DAY_PREFIX = "abuse:friend-req:user:";
    /** Redis 键前缀：按 IP 的每日好友申请次数 */
    private static final String FRIEND_IP_DAY_PREFIX = "abuse:friend-req:ip:";
    /** 自然日键片段，格式 yyyyMMdd（服务器本地时区） */
    private static final DateTimeFormatter DAY_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;

    private final RedisTemplate<String, Object> redisTemplate;
    private final LinkxSecurityProperties linkxSecurityProperties;
    /** 与登录限流共用：解析真实客户端 IP（含可信代理 X-Forwarded-For） */
    private final AuthSecurityGuard authSecurityGuard;

    /**
     * 用户搜索接口调用前校验（按用户 + 按 IP 双维度滑动窗口）。
     *
     * @param userId  当前登录用户 ID
     * @param request 用于解析 IP；可为 null（则跳过 IP 维度）
     */
    public void checkUserSearch(Long userId, HttpServletRequest request) {
        LinkxSecurityProperties.AbuseProtection config = linkxSecurityProperties.getAbuseProtection();
        if (!config.isEnabled()) {
            return;
        }
        LinkxSecurityProperties.AbuseProtection.UserSearch limits = config.getUserSearch();
        long windowSeconds = Math.max(limits.getWindowSeconds(), 1);
        Duration window = Duration.ofSeconds(windowSeconds);

        if (userId != null && limits.getMaxRequestsPerUser() > 0) {
            ensureWithinSlidingWindow(
                    SEARCH_USER_PREFIX + userId,
                    limits.getMaxRequestsPerUser(),
                    window,
                    "user_search",
                    "userId=" + userId
            );
        }
        if (request != null && limits.getMaxRequestsPerIp() > 0) {
            String clientIp = authSecurityGuard.resolveClientIp(request);
            ensureWithinSlidingWindow(
                    SEARCH_IP_PREFIX + clientIp,
                    limits.getMaxRequestsPerIp(),
                    window,
                    "user_search_ip",
                    "clientIp=" + clientIp
            );
        }
    }

    /**
     * 发送好友申请前校验（按用户 + 按 IP 双维度自然日计数）。
     */
    public void checkFriendRequestSend(Long userId, HttpServletRequest request) {
        LinkxSecurityProperties.AbuseProtection config = linkxSecurityProperties.getAbuseProtection();
        if (!config.isEnabled()) {
            return;
        }
        LinkxSecurityProperties.AbuseProtection.FriendRequest limits = config.getFriendRequest();
        String day = LocalDate.now().format(DAY_FORMAT);
        Duration untilEndOfDay = durationUntilEndOfLocalDay();

        if (userId != null && limits.getMaxPerUserPerDay() > 0) {
            ensureWithinDailyCounter(
                    FRIEND_USER_DAY_PREFIX + day + ":" + userId,
                    limits.getMaxPerUserPerDay(),
                    untilEndOfDay,
                    "friend_request",
                    "userId=" + userId
            );
        }
        if (request != null && limits.getMaxPerIpPerDay() > 0) {
            String clientIp = authSecurityGuard.resolveClientIp(request);
            ensureWithinDailyCounter(
                    FRIEND_IP_DAY_PREFIX + day + ":" + clientIp,
                    limits.getMaxPerIpPerDay(),
                    untilEndOfDay,
                    "friend_request_ip",
                    "clientIp=" + clientIp
            );
        }
    }

    /**
     * 滑动窗口：INCR 后若首次写入则设置 TTL；超过 max 则拒绝。
     */
    private void ensureWithinSlidingWindow(String redisKey, long maxRequests, Duration window, String scene, String detail) {
        Long count = redisTemplate.opsForValue().increment(redisKey);
        if (count != null && count == 1L) {
            redisTemplate.expire(redisKey, window);
        }
        if (count != null && count > maxRequests) {
            log.warn("Abuse protection blocked, scene={}, {}, count={}, max={}", scene, detail, count, maxRequests);
            throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS, "操作过于频繁，请稍后再试");
        }
    }

    /**
     * 自然日计数：键在当日结束时过期。
     */
    private void ensureWithinDailyCounter(String redisKey, long maxRequests, Duration ttl, String scene, String detail) {
        Long count = redisTemplate.opsForValue().increment(redisKey);
        if (count != null && count == 1L) {
            redisTemplate.expire(redisKey, ttl);
        }
        if (count != null && count > maxRequests) {
            log.warn("Abuse protection daily limit blocked, scene={}, {}, count={}, max={}", scene, detail, count, maxRequests);
            throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS, "今日操作次数已达上限，请明天再试");
        }
    }

    /** 从当前时刻到本地次日 0 点的剩余时间，用作 Redis 过期 */
    private Duration durationUntilEndOfLocalDay() {
        LocalDate today = LocalDate.now();
        java.time.LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();
        Duration remaining = Duration.between(java.time.LocalDateTime.now(), endOfDay);
        if (remaining.isNegative() || remaining.isZero()) {
            return Duration.ofHours(1);
        }
        return remaining;
    }
}