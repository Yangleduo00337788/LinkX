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
 * 登出后的 access token 黑名单（Redis）。
 * <p>
 * Key 为 token 的 SHA-256 摘要，TTL 与令牌剩余有效期一致，避免 Redis 长期堆积。
 * </p>
 */
@Service  // 行注：应用 @Service 注解
@RequiredArgsConstructor  // 行注：应用 @RequiredArgsConstructor 注解
// 行注：定义 AccessTokenDenylistService 类
public class AccessTokenDenylistService {

    private static final String KEY_PREFIX = "auth:access-deny:";  // 行注：定义键PREFIX常量

    private final RedisTemplate<String, Object> redisTemplate;  // 行注：注入RedisTemplate依赖
    private final JwtTokenProvider jwtTokenProvider;  // 行注：注入JWT令牌提供器依赖

    /** 将 access 加入黑名单直至其自然过期 */
    // 行注：定义deny令牌方法
    public void denyToken(String accessToken) {
        // 行注：判断是否满足当前条件
        if (!StringUtils.hasText(accessToken)) {
            return;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        long ttlMillis = jwtTokenProvider.getRemainingValidityMillis(accessToken.trim());  // 行注：初始化TTLMillis
        // 行注：判断是否满足当前条件
        if (ttlMillis <= 0) {
            return;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        redisTemplate.opsForValue().set(buildKey(accessToken), "1", Duration.ofMillis(ttlMillis));  // 行注：调用ops值
    }  // 行注：结束当前代码块

    /** 过滤器在鉴权前查询 access 是否已登出注销 */
    // 行注：定义是否Denied方法
    public boolean isDenied(String accessToken) {
        // 行注：判断是否满足当前条件
        if (!StringUtils.hasText(accessToken)) {
            return false;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        return Boolean.TRUE.equals(redisTemplate.hasKey(buildKey(accessToken)));  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义构建键方法
    private String buildKey(String accessToken) {
        return KEY_PREFIX + hashToken(accessToken);  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义hash令牌方法
    private String hashToken(String accessToken) {
        // 行注：尝试执行可能失败的逻辑
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");  // 行注：初始化摘要器
            byte[] encoded = digest.digest(accessToken.trim().getBytes(StandardCharsets.UTF_8));  // 行注：初始化编码结果
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
