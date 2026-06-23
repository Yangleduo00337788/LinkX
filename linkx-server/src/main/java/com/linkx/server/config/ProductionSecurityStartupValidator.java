package com.linkx.server.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

/**
 * {@code prod} profile 启动完成后强制校验关键安全配置，不满足则抛异常终止进程。
 * <p>
 * 避免误用默认 JWT、空 CORS、关闭验证码等上线。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductionSecurityStartupValidator {

    private static final String DEFAULT_JWT_PLACEHOLDER = "PLEASE_CHANGE_ME_LINKX_JWT_SECRET_2026_32CHARS_MIN";

    private final LinkxSecurityProperties linkxSecurityProperties;
    private final Environment environment;

    @EventListener(ApplicationReadyEvent.class)
    public void validateProductionSecurity() {
        if (!isProductionProfile()) {
            return;
        }
        validateCors();
        validateJwtSecret();
        validateCaptcha();
        validateDatabasePassword();
        validateRedisPassword();
    }

    private void validateCors() {
        List<String> origins = linkxSecurityProperties.getAllowedOriginPatterns();
        if (origins == null || origins.isEmpty()) {
            throw new IllegalStateException(
                    "生产环境必须配置 linkx.security.allowed-origin-patterns（环境变量 LINKX_SECURITY_ALLOWED_ORIGIN_PATTERNS）"
            );
        }
        for (String pattern : origins) {
            if (!StringUtils.hasText(pattern)) {
                continue;
            }
            if ("*".equals(pattern.trim())) {
                throw new IllegalStateException("生产环境不允许 CORS 使用通配符 *");
            }
        }
        log.info("Production CORS validated, allowedOriginPatternCount={}", origins.size());
    }

    private void validateJwtSecret() {
        String secret = environment.getProperty("linkx.jwt.secret");
        if (!StringUtils.hasText(secret) || DEFAULT_JWT_PLACEHOLDER.equals(secret.trim())) {
            throw new IllegalStateException("生产环境必须通过环境变量 LINKX_JWT_SECRET 配置 JWT 密钥");
        }
    }

    private void validateCaptcha() {
        if (!linkxSecurityProperties.getCaptcha().isEnabled()) {
            throw new IllegalStateException("生产环境必须启用验证码（LINKX_SECURITY_CAPTCHA_ENABLED=true）");
        }
    }

    private void validateDatabasePassword() {
        String password = environment.getProperty("spring.datasource.password");
        if (!StringUtils.hasText(password)) {
            throw new IllegalStateException("生产环境必须配置数据库密码（LINKX_DB_PASSWORD）");
        }
    }

    /** Redis 无密码仅告警（内网 Docker 场景可能接受，公网暴露则必须设密） */
    private void validateRedisPassword() {
        String password = environment.getProperty("spring.data.redis.password");
        if (!StringUtils.hasText(password)) {
            log.warn("Production Redis password is empty; configure LINKX_REDIS_PASSWORD when Redis is exposed");
        }
    }

    private boolean isProductionProfile() {
        return Arrays.stream(environment.getActiveProfiles())
                .anyMatch(profile -> "prod".equalsIgnoreCase(profile));
    }
}