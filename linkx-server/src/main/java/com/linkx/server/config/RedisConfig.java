package com.linkx.server.config;  // 行注：声明当前文件所在包 com.linkx.server.config

import org.springframework.context.annotation.Bean;  // 行注：引入 Bean 类型
import org.springframework.context.annotation.Configuration;  // 行注：引入 Configuration 类型
import org.springframework.data.redis.connection.RedisConnectionFactory;  // 行注：引入 RedisConnectionFactory 类型
import org.springframework.data.redis.core.RedisTemplate;  // 行注：引入 RedisTemplate 类型
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;  // 行注：引入 GenericJackson2JsonRedisSerializer 类型
import org.springframework.data.redis.serializer.StringRedisSerializer;  // 行注：引入 StringRedisSerializer 类型

/**
 * Redis 模板：Key 为字符串，Value 为 JSON（限流计数、验证码、refresh 会话、token 黑名单等）。
 */
@Configuration  // 行注：应用 @Configuration 注解
// 行注：定义 RedisConfig 类
public class RedisConfig {

    /**
     * 通用 Redis 操作模板，供限流、验证码、令牌黑名单等业务注入使用。
     *
     * @param connectionFactory Spring Boot 自动配置的连接工厂
     */
    @Bean  // 行注：应用 @Bean 注解
    // 行注：定义RedisTemplate方法
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();  // 行注：初始化template
        template.setConnectionFactory(connectionFactory);  // 行注：调用设置ConnectionFactory
        template.setKeySerializer(new StringRedisSerializer());  // 行注：调用设置键Serializer
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());  // 行注：调用设置值Serializer
        template.setHashKeySerializer(new StringRedisSerializer());  // 行注：调用设置Hash键Serializer
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());  // 行注：调用设置Hash值Serializer
        template.afterPropertiesSet();  // 行注：调用after属性设置
        return template;  // 行注：返回处理结果
    }  // 行注：结束当前代码块
}  // 行注：结束当前代码块
