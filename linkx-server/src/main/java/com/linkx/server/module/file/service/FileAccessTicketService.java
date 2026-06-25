package com.linkx.server.module.file.service;  // 行注：声明当前文件所在包 com.linkx.server.module.file.service

import com.linkx.server.entity.SysFile;  // 行注：引入 SysFile 类型
import com.linkx.server.mapper.SysFileMapper;  // 行注：引入 SysFileMapper 类型
import lombok.RequiredArgsConstructor;  // 行注：引入 RequiredArgsConstructor 类型
import org.springframework.data.redis.core.RedisTemplate;  // 行注：引入 RedisTemplate 类型
import org.springframework.stereotype.Service;  // 行注：引入 Service 类型
import org.springframework.util.StringUtils;  // 行注：引入 StringUtils 类型

import java.security.SecureRandom;  // 行注：引入 SecureRandom 类型
import java.time.Duration;  // 行注：引入 Duration 类型
import java.util.Base64;  // 行注：引入 Base64 类型

/**
 * 文件访问一次性 ticket（Redis，默认 10 分钟）：校验 userId 后消费并返回 {@link SysFile}。
 */
@Service  // 行注：应用 @Service 注解
@RequiredArgsConstructor  // 行注：应用 @RequiredArgsConstructor 注解
// 行注：定义 FileAccessTicketService 类
public class FileAccessTicketService {

    private static final long TICKET_TTL_MILLIS = 10 * 60_000L;  // 行注：定义票据TTLMILLIS常量
    private static final String KEY_PREFIX = "file:access-ticket:";  // 行注：定义键PREFIX常量

    private final SysFileMapper fileMapper;  // 行注：注入文件Mapper依赖
    private final RedisTemplate<String, Object> redisTemplate;  // 行注：注入RedisTemplate依赖
    private final SecureRandom secureRandom = new SecureRandom();  // 行注：注入secureRandom依赖

    /**
     * 创建票据。
     *
     * @param userId 用户 ID
     * @param fileId 文件 ID
     * @return 字符串结果
     */
    // 行注：定义创建票据方法
    public String createTicket(Long userId, Long fileId) {
        // 行注：判断是否满足当前条件
        if (userId == null || fileId == null) {
            throw new IllegalArgumentException("userId and fileId are required");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
        byte[] randomBytes = new byte[24];  // 行注：初始化randomBytes
        secureRandom.nextBytes(randomBytes);  // 行注：调用nextBytes
        String ticket = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);  // 行注：初始化票据
        String payload = userId + ":" + fileId;  // 行注：初始化载荷
        redisTemplate.opsForValue().set(buildKey(ticket), payload, Duration.ofMillis(TICKET_TTL_MILLIS));  // 行注：调用ops值
        return ticket;  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /**
     * Validates ticket, checks requester matches ticket owner, loads file, then consumes ticket (single use).
     */
    // 行注：定义consume文件方法
    public SysFile consumeFile(String ticket, Long requestUserId) {
        // 行注：判断是否满足当前条件
        if (requestUserId == null) {
            return null;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        // 行注：判断是否满足当前条件
        if (!StringUtils.hasText(ticket)) {
            return null;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        String redisKey = buildKey(ticket.trim());  // 行注：初始化Redis键
        Object cached = redisTemplate.opsForValue().get(redisKey);  // 行注：初始化cached
        // 行注：判断是否满足当前条件
        if (!(cached instanceof String payload) || !StringUtils.hasText(payload)) {
            return null;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        int separator = payload.indexOf(':');  // 行注：初始化separator
        // 行注：判断是否满足当前条件
        if (separator <= 0 || separator >= payload.length() - 1) {
            redisTemplate.delete(redisKey);  // 行注：调用删除
            return null;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        // 行注：尝试执行可能失败的逻辑
        try {
            long ticketUserId = Long.parseLong(payload.substring(0, separator));  // 行注：初始化票据用户ID
            long fileId = Long.parseLong(payload.substring(separator + 1));  // 行注：初始化文件ID
            // 行注：判断是否满足当前条件
            if (ticketUserId != requestUserId) {
                return null;  // 行注：返回处理结果
            }  // 行注：结束当前代码块
            SysFile sysFile = fileMapper.selectById(fileId);  // 行注：初始化系统文件
            // 行注：判断是否满足当前条件
            if (sysFile == null) {
                redisTemplate.delete(redisKey);  // 行注：调用删除
                return null;  // 行注：返回处理结果
            }  // 行注：结束当前代码块
            redisTemplate.delete(redisKey);  // 行注：调用删除
            return sysFile;  // 行注：返回处理结果
        // 行注：执行当前方法调用
        } catch (NumberFormatException exception) {
            redisTemplate.delete(redisKey);  // 行注：调用删除
            return null;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
    }  // 行注：结束当前代码块

    // 行注：定义构建键方法
    private String buildKey(String ticket) {
        return KEY_PREFIX + ticket.trim();  // 行注：返回处理结果
    }  // 行注：结束当前代码块
}  // 行注：结束当前代码块
