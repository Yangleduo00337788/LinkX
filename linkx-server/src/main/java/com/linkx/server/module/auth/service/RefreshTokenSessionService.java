package com.linkx.server.module.auth.service;  // 行注：声明当前文件所在包 com.linkx.server.module.auth.service

import com.linkx.server.security.JwtTokenProvider;  // 行注：引入 JwtTokenProvider 类型
import lombok.RequiredArgsConstructor;  // 行注：引入 RequiredArgsConstructor 类型
import org.springframework.data.redis.core.RedisTemplate;  // 行注：引入 RedisTemplate 类型
import org.springframework.stereotype.Service;  // 行注：引入 Service 类型
import org.springframework.util.StringUtils;  // 行注：引入 StringUtils 类型

import java.nio.charset.StandardCharsets;  // 行注：引入 StandardCharsets 类型
import java.security.MessageDigest;  // 行注：引入 MessageDigest 类型
import java.security.NoSuchAlgorithmException;  // 行注：引入 NoSuchAlgorithmException 类型
import java.time.Duration;  // 行注：引入 Duration 类型

/**
 * 每用户仅保留一份有效 refresh：Redis 存 refresh 的 SHA-256 哈希。
 * <p>
 * 刷新或登出会轮换/删除；用 access 调 refresh 接口无效。
 * </p>
 */
@Service  // 行注：应用 @Service 注解
@RequiredArgsConstructor  // 行注：应用 @RequiredArgsConstructor 注解
// 行注：定义 RefreshTokenSessionService 类
public class RefreshTokenSessionService {

    private static final String KEY_PREFIX = "auth:refresh-token:";  // 行注：定义键PREFIX常量

    private final RedisTemplate<String, Object> redisTemplate;  // 行注：注入RedisTemplate依赖
    private final JwtTokenProvider jwtTokenProvider;  // 行注：注入JWT令牌提供器依赖

    /** 登录/注册/刷新成功后写入当前 refresh 哈希 */
    // 行注：定义申请令牌方法
    public void issueToken(Long userId, String refreshToken) {
        // 行注：判断是否满足当前条件
        if (userId == null || !StringUtils.hasText(refreshToken)) {
            return;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        long ttlMillis = jwtTokenProvider.getRemainingValidityMillis(refreshToken);  // 行注：初始化TTLMillis
        // 行注：判断是否满足当前条件
        if (ttlMillis <= 0) {
            revokeToken(userId);  // 行注：调用revoke令牌
            return;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        redisTemplate.opsForValue().set(buildKey(userId), hashToken(refreshToken), Duration.ofMillis(ttlMillis));  // 行注：调用ops值
    }  // 行注：结束当前代码块

    /** 防止使用已轮换的旧 refresh */
    // 行注：定义matches启用令牌方法
    public boolean matchesActiveToken(Long userId, String refreshToken) {
        // 行注：判断是否满足当前条件
        if (userId == null || !StringUtils.hasText(refreshToken)) {
            return false;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        String key = buildKey(userId);  // 行注：初始化键
        Object storedHash = redisTemplate.opsForValue().get(key);  // 行注：初始化已存储值Hash
        return storedHash != null && hashToken(refreshToken).equals(String.valueOf(storedHash));  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /** 登出或 refresh 轮换前删除 Redis 中的 refresh 会话 */
    // 行注：定义revoke令牌方法
    public void revokeToken(Long userId) {
        // 行注：判断是否满足当前条件
        if (userId == null) {
            return;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        redisTemplate.delete(buildKey(userId));  // 行注：调用删除
    }  // 行注：结束当前代码块

    // 行注：定义构建键方法
    private String buildKey(Long userId) {
        return KEY_PREFIX + userId;  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义hash令牌方法
    private String hashToken(String refreshToken) {
        // 行注：尝试执行可能失败的逻辑
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");  // 行注：初始化摘要器
            byte[] encoded = digest.digest(refreshToken.trim().getBytes(StandardCharsets.UTF_8));  // 行注：初始化编码结果
            StringBuilder builder = new StringBuilder(encoded.length * 2);  // 行注：初始化构建器
            // 行注：遍历当前集合或范围
            for (byte value : encoded) {
                builder.append(String.format("%02x", value));  // 行注：调用append
            }  // 行注：结束当前代码块
            return builder.toString();  // 行注：返回处理结果
        // 行注：执行当前方法调用
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm unavailable", exception);  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
    }  // 行注：结束当前代码块
}  // 行注：结束当前代码块
