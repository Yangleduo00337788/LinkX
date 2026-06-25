package com.linkx.server.config;  // 行注：声明当前文件所在包 com.linkx.server.config

import lombok.RequiredArgsConstructor;  // 行注：引入 RequiredArgsConstructor 类型
import lombok.extern.slf4j.Slf4j;  // 行注：引入 Slf4j 类型
import org.springframework.boot.context.event.ApplicationReadyEvent;  // 行注：引入 ApplicationReadyEvent 类型
import org.springframework.context.event.EventListener;  // 行注：引入 EventListener 类型
import org.springframework.core.env.Environment;  // 行注：引入 Environment 类型
import org.springframework.stereotype.Component;  // 行注：引入 Component 类型
import org.springframework.util.StringUtils;  // 行注：引入 StringUtils 类型

import java.util.Arrays;  // 行注：引入 Arrays 类型
import java.util.List;  // 行注：引入 List 类型

/**
 * {@code prod} profile 启动完成后强制校验关键安全配置，不满足则抛异常终止进程。
 * <p>
 * 避免误用默认 JWT、空 CORS、关闭验证码等上线。
 * </p>
 */
@Slf4j  // 行注：应用 @Slf4j 注解
@Component  // 行注：应用 @Component 注解
@RequiredArgsConstructor  // 行注：应用 @RequiredArgsConstructor 注解
// 行注：定义 ProductionSecurityStartupValidator 类
public class ProductionSecurityStartupValidator {

    private static final String DEFAULT_JWT_PLACEHOLDER = "PLEASE_CHANGE_ME_LINKX_JWT_SECRET_2026_32CHARS_MIN";  // 行注：定义默认JWTPLACEHOLDER常量

    private final LinkxSecurityProperties linkxSecurityProperties;  // 行注：注入LinkX 安全属性依赖
    private final Environment environment;  // 行注：注入environment依赖

    /**
     * 校验生产环境安全配置。
     */
    @EventListener(ApplicationReadyEvent.class)  // 行注：应用 @EventListener 注解
    // 行注：定义validateProduction安全方法
    public void validateProductionSecurity() {
        // 行注：判断是否满足当前条件
        if (!isProductionProfile()) {
            return;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        validateCors();  // 行注：调用validateCORS
        validateJwtSecret();  // 行注：调用validateJWT密钥
        validateCaptcha();  // 行注：调用validate验证码
        validateDatabasePassword();  // 行注：调用validateDatabase密码
        validateRedisPassword();  // 行注：调用validateRedis密码
    }  // 行注：结束当前代码块

    // 行注：定义validateCORS方法
    private void validateCors() {
        List<String> origins = linkxSecurityProperties.getAllowedOriginPatterns();  // 行注：初始化origins
        // 行注：判断是否满足当前条件
        if (origins == null || origins.isEmpty()) {
            throw new IllegalStateException(  // 行注：抛出异常并中断当前流程
                    // 行注：补充当前表达式片段
                    "生产环境必须配置 linkx.security.allowed-origin-patterns（环境变量 LINKX_SECURITY_ALLOWED_ORIGIN_PATTERNS）"
            );  // 行注：结束当前参数配置
        }  // 行注：结束当前代码块
        // 行注：遍历当前集合或范围
        for (String pattern : origins) {
            // 行注：判断是否满足当前条件
            if (!StringUtils.hasText(pattern)) {
                continue;  // 行注：完成当前语句
            }  // 行注：结束当前代码块
            // 行注：判断是否满足当前条件
            if ("*".equals(pattern.trim())) {
                throw new IllegalStateException("生产环境不允许 CORS 使用通配符 *");  // 行注：抛出异常并中断当前流程
            }  // 行注：结束当前代码块
        }  // 行注：结束当前代码块
        log.info("Production CORS validated, allowedOriginPatternCount={}", origins.size());  // 行注：执行初始化操作
    }  // 行注：结束当前代码块

    // 行注：定义validateJWT密钥方法
    private void validateJwtSecret() {
        String secret = environment.getProperty("linkx.jwt.secret");  // 行注：初始化密钥
        // 行注：判断是否满足当前条件
        if (!StringUtils.hasText(secret) || DEFAULT_JWT_PLACEHOLDER.equals(secret.trim())) {
            throw new IllegalStateException("生产环境必须通过环境变量 LINKX_JWT_SECRET 配置 JWT 密钥");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
    }  // 行注：结束当前代码块

    // 行注：定义validate验证码方法
    private void validateCaptcha() {
        // 行注：判断是否满足当前条件
        if (!linkxSecurityProperties.getCaptcha().isEnabled()) {
            throw new IllegalStateException("生产环境必须启用验证码（LINKX_SECURITY_CAPTCHA_ENABLED=true）");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
    }  // 行注：结束当前代码块

    // 行注：定义validateDatabase密码方法
    private void validateDatabasePassword() {
        String password = environment.getProperty("spring.datasource.password");  // 行注：初始化密码
        // 行注：判断是否满足当前条件
        if (!StringUtils.hasText(password)) {
            throw new IllegalStateException("生产环境必须配置数据库密码（LINKX_DB_PASSWORD）");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
    }  // 行注：结束当前代码块

    /** Redis 无密码仅告警（内网 Docker 场景可能接受，公网暴露则必须设密） */
    // 行注：定义validateRedis密码方法
    private void validateRedisPassword() {
        String password = environment.getProperty("spring.data.redis.password");  // 行注：初始化密码
        // 行注：判断是否满足当前条件
        if (!StringUtils.hasText(password)) {
            log.warn("Production Redis password is empty; configure LINKX_REDIS_PASSWORD when Redis is exposed");  // 行注：调用警告日志
        }  // 行注：结束当前代码块
    }  // 行注：结束当前代码块

    // 行注：定义是否ProductionProfile方法
    private boolean isProductionProfile() {
        return Arrays.stream(environment.getActiveProfiles())  // 行注：返回处理结果
                .anyMatch(profile -> "prod".equalsIgnoreCase(profile));  // 行注：继续调用任意Match
    }  // 行注：结束当前代码块
}  // 行注：结束当前代码块
