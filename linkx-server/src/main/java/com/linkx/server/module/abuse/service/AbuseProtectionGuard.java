package com.linkx.server.module.abuse.service;  // 行注：声明当前文件所在包 com.linkx.server.module.abuse.service

import com.linkx.server.common.BusinessException;  // 行注：引入 BusinessException 类型
import com.linkx.server.common.ErrorCode;  // 行注：引入 ErrorCode 类型
import com.linkx.server.config.LinkxSecurityProperties;  // 行注：引入 LinkxSecurityProperties 类型
import com.linkx.server.module.auth.service.AuthSecurityGuard;  // 行注：引入 AuthSecurityGuard 类型
import jakarta.servlet.http.HttpServletRequest;  // 行注：引入 HttpServletRequest 类型
import lombok.RequiredArgsConstructor;  // 行注：引入 RequiredArgsConstructor 类型
import lombok.extern.slf4j.Slf4j;  // 行注：引入 Slf4j 类型
import org.springframework.data.redis.core.RedisTemplate;  // 行注：引入 RedisTemplate 类型
import org.springframework.stereotype.Service;  // 行注：引入 Service 类型

import java.time.Duration;  // 行注：引入 Duration 类型
import java.time.LocalDate;  // 行注：引入 LocalDate 类型
import java.time.format.DateTimeFormatter;  // 行注：引入 DateTimeFormatter 类型

/**
 * 公网滥用防护：用户搜索、发送好友申请的频率与每日上限。
 * <p>
 * 计数存储在 Redis；配置项见 {@link LinkxSecurityProperties#getAbuseProtection()}。
 * 超限抛出 {@link BusinessException}，错误码 {@link ErrorCode#TOO_MANY_REQUESTS}。
 * </p>
 */
@Slf4j  // 行注：应用 @Slf4j 注解
@Service  // 行注：应用 @Service 注解
@RequiredArgsConstructor  // 行注：应用 @RequiredArgsConstructor 注解
// 行注：定义 AbuseProtectionGuard 类
public class AbuseProtectionGuard {

    /** Redis 键前缀：按用户 ID 的搜索次数（滑动窗口） */
    private static final String SEARCH_USER_PREFIX = "abuse:search:user:";  // 行注：定义搜索用户PREFIX常量
    /** Redis 键前缀：按客户端 IP 的搜索次数 */
    private static final String SEARCH_IP_PREFIX = "abuse:search:ip:";  // 行注：定义搜索IPPREFIX常量
    /** Redis 键前缀：按用户 ID 的每日好友申请次数 */
    private static final String FRIEND_USER_DAY_PREFIX = "abuse:friend-req:user:";  // 行注：定义好友用户日期PREFIX常量
    /** Redis 键前缀：按 IP 的每日好友申请次数 */
    private static final String FRIEND_IP_DAY_PREFIX = "abuse:friend-req:ip:";  // 行注：定义好友IP日期PREFIX常量
    /** 自然日键片段，格式 yyyyMMdd（服务器本地时区） */
    private static final DateTimeFormatter DAY_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;  // 行注：定义日期格式化常量

    private final RedisTemplate<String, Object> redisTemplate;  // 行注：注入RedisTemplate依赖
    private final LinkxSecurityProperties linkxSecurityProperties;  // 行注：注入LinkX 安全属性依赖
    /** 与登录限流共用：解析真实客户端 IP（含可信代理 X-Forwarded-For） */
    private final AuthSecurityGuard authSecurityGuard;  // 行注：注入认证安全Guard依赖

    /**
     * 用户搜索接口调用前校验（按用户 + 按 IP 双维度滑动窗口）。
     *
     * @param userId  当前登录用户 ID
     * @param request 用于解析 IP；可为 null（则跳过 IP 维度）
     */
    // 行注：定义检查用户搜索方法
    public void checkUserSearch(Long userId, HttpServletRequest request) {
        LinkxSecurityProperties.AbuseProtection config = linkxSecurityProperties.getAbuseProtection();  // 行注：执行初始化操作
        // 行注：判断是否满足当前条件
        if (!config.isEnabled()) {
            return;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        LinkxSecurityProperties.AbuseProtection.UserSearch limits = config.getUserSearch();  // 行注：执行初始化操作
        long windowSeconds = Math.max(limits.getWindowSeconds(), 1);  // 行注：初始化窗口Seconds
        Duration window = Duration.ofSeconds(windowSeconds);  // 行注：初始化窗口

        // 行注：判断是否满足当前条件
        if (userId != null && limits.getMaxRequestsPerUser() > 0) {
            // 行注：补充当前表达式片段
            ensureWithinSlidingWindow(
                    // 行注：补充当前表达式片段
                    SEARCH_USER_PREFIX + userId,
                    // 行注：调用获取最大请求Per用户
                    limits.getMaxRequestsPerUser(),
                    // 行注：补充当前表达式片段
                    window,
                    // 行注：补充当前表达式片段
                    "user_search",
                    // 行注：补充当前表达式片段
                    "userId=" + userId
            );  // 行注：结束当前参数配置
        }  // 行注：结束当前代码块
        // 行注：判断是否满足当前条件
        if (request != null && limits.getMaxRequestsPerIp() > 0) {
            String clientIp = authSecurityGuard.resolveClientIp(request);  // 行注：初始化客户端IP
            // 行注：补充当前表达式片段
            ensureWithinSlidingWindow(
                    // 行注：补充当前表达式片段
                    SEARCH_IP_PREFIX + clientIp,
                    // 行注：调用获取最大请求PerIP
                    limits.getMaxRequestsPerIp(),
                    // 行注：补充当前表达式片段
                    window,
                    // 行注：补充当前表达式片段
                    "user_search_ip",
                    // 行注：补充当前表达式片段
                    "clientIp=" + clientIp
            );  // 行注：结束当前参数配置
        }  // 行注：结束当前代码块
    }  // 行注：结束当前代码块

    /**
     * 发送好友申请前校验（按用户 + 按 IP 双维度自然日计数）。
     */
    // 行注：定义检查好友请求发送方法
    public void checkFriendRequestSend(Long userId, HttpServletRequest request) {
        LinkxSecurityProperties.AbuseProtection config = linkxSecurityProperties.getAbuseProtection();  // 行注：执行初始化操作
        // 行注：判断是否满足当前条件
        if (!config.isEnabled()) {
            return;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        LinkxSecurityProperties.AbuseProtection.FriendRequest limits = config.getFriendRequest();  // 行注：执行初始化操作
        String day = LocalDate.now().format(DAY_FORMAT);  // 行注：初始化日期
        Duration untilEndOfDay = durationUntilEndOfLocalDay();  // 行注：初始化untilEndOf日期

        // 行注：判断是否满足当前条件
        if (userId != null && limits.getMaxPerUserPerDay() > 0) {
            // 行注：补充当前表达式片段
            ensureWithinDailyCounter(
                    // 行注：补充当前表达式片段
                    FRIEND_USER_DAY_PREFIX + day + ":" + userId,
                    // 行注：调用获取最大Per用户Per日期
                    limits.getMaxPerUserPerDay(),
                    // 行注：补充当前表达式片段
                    untilEndOfDay,
                    // 行注：补充当前表达式片段
                    "friend_request",
                    // 行注：补充当前表达式片段
                    "userId=" + userId
            );  // 行注：结束当前参数配置
        }  // 行注：结束当前代码块
        // 行注：判断是否满足当前条件
        if (request != null && limits.getMaxPerIpPerDay() > 0) {
            String clientIp = authSecurityGuard.resolveClientIp(request);  // 行注：初始化客户端IP
            // 行注：补充当前表达式片段
            ensureWithinDailyCounter(
                    // 行注：补充当前表达式片段
                    FRIEND_IP_DAY_PREFIX + day + ":" + clientIp,
                    // 行注：调用获取最大PerIPPer日期
                    limits.getMaxPerIpPerDay(),
                    // 行注：补充当前表达式片段
                    untilEndOfDay,
                    // 行注：补充当前表达式片段
                    "friend_request_ip",
                    // 行注：补充当前表达式片段
                    "clientIp=" + clientIp
            );  // 行注：结束当前参数配置
        }  // 行注：结束当前代码块
    }  // 行注：结束当前代码块

    /**
     * 滑动窗口：INCR 后若首次写入则设置 TTL；超过 max 则拒绝。
     */
    // 行注：定义ensureWithinSliding窗口方法
    private void ensureWithinSlidingWindow(String redisKey, long maxRequests, Duration window, String scene, String detail) {
        Long count = redisTemplate.opsForValue().increment(redisKey);  // 行注：初始化数量
        // 行注：判断是否满足当前条件
        if (count != null && count == 1L) {
            redisTemplate.expire(redisKey, window);  // 行注：调用expire
        }  // 行注：结束当前代码块
        // 行注：判断是否满足当前条件
        if (count != null && count > maxRequests) {
            log.warn("Abuse protection blocked, scene={}, {}, count={}, max={}", scene, detail, count, maxRequests);  // 行注：执行初始化操作
            throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS, "操作过于频繁，请稍后再试");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
    }  // 行注：结束当前代码块

    /**
     * 自然日计数：键在当日结束时过期。
     */
    // 行注：定义ensureWithinDailyCounter方法
    private void ensureWithinDailyCounter(String redisKey, long maxRequests, Duration ttl, String scene, String detail) {
        Long count = redisTemplate.opsForValue().increment(redisKey);  // 行注：初始化数量
        // 行注：判断是否满足当前条件
        if (count != null && count == 1L) {
            redisTemplate.expire(redisKey, ttl);  // 行注：调用expire
        }  // 行注：结束当前代码块
        // 行注：判断是否满足当前条件
        if (count != null && count > maxRequests) {
            // 行注：执行初始化操作
            log.warn("Abuse protection daily limit blocked, scene={}, {}, count={}, max={}", scene, detail, count, maxRequests);
            throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS, "今日操作次数已达上限，请明天再试");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
    }  // 行注：结束当前代码块

    /** 从当前时刻到本地次日 0 点的剩余时间，用作 Redis 过期 */
    // 行注：定义durationUntilEndOf本地日期方法
    private Duration durationUntilEndOfLocalDay() {
        LocalDate today = LocalDate.now();  // 行注：初始化当天日期
        java.time.LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();  // 行注：执行初始化操作
        Duration remaining = Duration.between(java.time.LocalDateTime.now(), endOfDay);  // 行注：初始化剩余时长
        // 行注：判断是否满足当前条件
        if (remaining.isNegative() || remaining.isZero()) {
            return Duration.ofHours(1);  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        return remaining;  // 行注：返回处理结果
    }  // 行注：结束当前代码块
}  // 行注：结束当前代码块
