package com.linkx.server.module.chat.ws;  // 行注：声明当前文件所在包 com.linkx.server.module.chat.ws

import lombok.RequiredArgsConstructor;  // 行注：引入 RequiredArgsConstructor 类型
import org.springframework.data.redis.core.RedisTemplate;  // 行注：引入 RedisTemplate 类型
import org.springframework.stereotype.Service;  // 行注：引入 Service 类型
import org.springframework.util.StringUtils;  // 行注：引入 StringUtils 类型

import java.security.SecureRandom;  // 行注：引入 SecureRandom 类型
import java.time.Duration;  // 行注：引入 Duration 类型
import java.util.Base64;  // 行注：引入 Base64 类型

/**
 * WebSocket 连接 ticket（Redis，约 60 秒）：REST 签发，握手时一次性消费，避免在 URL 中带 JWT。
 */
@Service  // 行注：应用 @Service 注解
@RequiredArgsConstructor  // 行注：应用 @RequiredArgsConstructor 注解
// 行注：定义 ChatWebSocketTicketService 类
public class ChatWebSocketTicketService {

    private static final long TICKET_TTL_MILLIS = 60_000L;  // 行注：定义票据TTLMILLIS常量
    private static final String KEY_PREFIX = "chat:ws-ticket:";  // 行注：定义键PREFIX常量

    private final RedisTemplate<String, Object> redisTemplate;  // 行注：注入RedisTemplate依赖
    private final SecureRandom secureRandom = new SecureRandom();  // 行注：注入secureRandom依赖

    /** 为已登录用户生成一次性 ticket，约 60 秒内有效 */
    // 行注：定义创建票据方法
    public String createTicket(Long userId) {
        // 使用安全随机数生成高熵票据，避免被猜测或枚举。
        byte[] randomBytes = new byte[24];  // 行注：初始化randomBytes
        secureRandom.nextBytes(randomBytes);  // 行注：调用nextBytes
        String ticket = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);  // 行注：初始化票据
        // ticket 只在 Redis 中短暂存活，并和 userId 绑定，握手时再换回真实身份。
        redisTemplate.opsForValue().set(buildKey(ticket), userId, Duration.ofMillis(TICKET_TTL_MILLIS));  // 行注：调用ops值
        return ticket;  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /** 握手时校验并删除 ticket，返回绑定的 userId；无效返回 null */
    // 行注：定义consume票据方法
    public Long consumeTicket(String ticket) {
        // 行注：判断是否满足当前条件
        if (!StringUtils.hasText(ticket)) {
            return null;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        // getAndDelete 保证 ticket 只能被成功消费一次，天然满足一次性握手凭证要求。
        Object cachedUserId = redisTemplate.opsForValue().getAndDelete(buildKey(ticket));  // 行注：初始化cached用户ID
        // 行注：判断是否满足当前条件
        if (cachedUserId instanceof Number number) {
            return number.longValue();  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        // 行注：判断是否满足当前条件
        if (cachedUserId instanceof String text && StringUtils.hasText(text)) {
            // 行注：尝试执行可能失败的逻辑
            try {
                // 兼容 Redis 序列化策略差异，字符串形式的 userId 也允许正常解析。
                return Long.parseLong(text.trim());  // 行注：返回处理结果
            // 行注：执行当前方法调用
            } catch (NumberFormatException ignored) {
                return null;  // 行注：返回处理结果
            }  // 行注：结束当前代码块
        }  // 行注：结束当前代码块
        return null;  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义构建键方法
    private String buildKey(String ticket) {
        return KEY_PREFIX + ticket.trim();  // 行注：返回处理结果
    }  // 行注：结束当前代码块
}  // 行注：结束当前代码块
