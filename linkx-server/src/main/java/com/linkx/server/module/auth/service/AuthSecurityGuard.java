package com.linkx.server.module.auth.service;  // 行注：声明当前文件所在包 com.linkx.server.module.auth.service

import com.linkx.server.common.BusinessException;  // 行注：引入 BusinessException 类型
import com.linkx.server.common.ErrorCode;  // 行注：引入 ErrorCode 类型
import com.linkx.server.config.LinkxSecurityProperties;  // 行注：引入 LinkxSecurityProperties 类型
import com.linkx.server.module.auth.dto.CaptchaMetaDTO;  // 行注：引入 CaptchaMetaDTO 类型
import com.linkx.server.module.config.service.RuntimeConfigService;
import jakarta.servlet.http.HttpServletRequest;  // 行注：引入 HttpServletRequest 类型
import lombok.RequiredArgsConstructor;  // 行注：引入 RequiredArgsConstructor 类型
import lombok.extern.slf4j.Slf4j;  // 行注：引入 Slf4j 类型
import org.springframework.data.redis.core.RedisTemplate;  // 行注：引入 RedisTemplate 类型
import org.springframework.stereotype.Service;  // 行注：引入 Service 类型
import org.springframework.util.StringUtils;  // 行注：引入 StringUtils 类型

import java.time.Duration;  // 行注：引入 Duration 类型
import java.util.List;  // 行注：引入 List 类型
import java.util.Locale;  // 行注：引入 Locale 类型

/**
 * 认证链路安全：验证码校验、Redis 滑动窗口限流、客户端 IP 解析。
 * <p>
 * IP 仅在 {@link LinkxSecurityProperties.TrustedProxy#enabled} 为 true 时信任 X-Forwarded-For。
 * </p>
 */
@Slf4j  // 行注：应用 @Slf4j 注解
@Service  // 行注：应用 @Service 注解
@RequiredArgsConstructor  // 行注：应用 @RequiredArgsConstructor 注解
// 行注：定义 AuthSecurityGuard 类
public class AuthSecurityGuard {

    private static final String RATE_LIMIT_KEY_PREFIX = "auth:rate-limit:";  // 行注：定义RATE限制键PREFIX常量
    private static final String UNKNOWN_CLIENT_IP = "unknown";  // 行注：定义UNKNOWN客户端IP常量

    private final RedisTemplate<String, Object> redisTemplate;  // 行注：注入RedisTemplate依赖
    private final LinkxSecurityProperties linkxSecurityProperties;  // 行注：注入LinkX 安全属性依赖
    private final CaptchaService captchaService;  // 行注：注入验证码服务依赖
    private final RuntimeConfigService runtimeConfigService;

    /** 登录场景校验并消费验证码（未开启验证码时直接通过） */
    // 行注：定义validate登录验证码方法
    public void validateLoginCaptcha(String captchaId, String captchaCode) {
        validateCaptcha("login", captchaId, captchaCode);  // 行注：调用validate验证码
    }  // 行注：结束当前代码块

    /** 注册场景校验并消费验证码 */
    // 行注：定义validate注册验证码方法
    public void validateRegisterCaptcha(String captchaId, String captchaCode) {
        validateCaptcha("register", captchaId, captchaCode);  // 行注：调用validate验证码
    }  // 行注：结束当前代码块

    /** 管理端登录场景校验并消费验证码 */
    public void validateAdminLoginCaptcha(String captchaId, String captchaCode) {
        validateCaptcha("admin-login", captchaId, captchaCode);
    }

    /** 按 IP + 用户名维度限制登录尝试次数 */
    // 行注：定义检查登录Rate限制方法
    public void checkLoginRateLimit(HttpServletRequest request, String username) {
        LinkxSecurityProperties.AuthRateLimit properties = linkxSecurityProperties.getAuthRateLimit();  // 行注：执行初始化操作
        // 行注：判断是否满足当前条件
        if (!properties.isEnabled()) {
            return;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        String clientIp = resolveClientIp(request);  // 行注：初始化客户端IP
        String normalizedUsername = normalizeKeyPart(username);  // 行注：初始化规范化后的Username
        String bucketKey = clientIp + ":" + normalizedUsername;  // 行注：初始化bucket键
        // 行注：调用获取登录最大请求
        ensureWithinLimit("login", bucketKey, properties.getLoginMaxRequests(), properties.getWindowSeconds(), clientIp);
    }  // 行注：结束当前代码块

    /** 按 IP + 用户名维度限制注册请求次数 */
    // 行注：定义检查注册Rate限制方法
    public void checkRegisterRateLimit(HttpServletRequest request, String username) {
        LinkxSecurityProperties.AuthRateLimit properties = linkxSecurityProperties.getAuthRateLimit();  // 行注：执行初始化操作
        // 行注：判断是否满足当前条件
        if (!properties.isEnabled()) {
            return;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        String clientIp = resolveClientIp(request);  // 行注：初始化客户端IP
        String normalizedUsername = normalizeKeyPart(username);  // 行注：初始化规范化后的Username
        String bucketKey = clientIp + ":" + normalizedUsername;  // 行注：初始化bucket键
        // 行注：调用获取注册最大请求
        ensureWithinLimit("register", bucketKey, properties.getRegisterMaxRequests(), properties.getWindowSeconds(), clientIp);
    }  // 行注：结束当前代码块

    /** 按客户端 IP 限制 refresh token 接口调用频率 */
    // 行注：定义检查刷新Rate限制方法
    public void checkRefreshRateLimit(HttpServletRequest request) {
        LinkxSecurityProperties.AuthRateLimit properties = linkxSecurityProperties.getAuthRateLimit();  // 行注：执行初始化操作
        // 行注：判断是否满足当前条件
        if (!properties.isEnabled()) {
            return;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        String clientIp = resolveClientIp(request);  // 行注：初始化客户端IP
        // 行注：调用获取刷新最大请求
        ensureWithinLimit("refresh", clientIp, properties.getRefreshMaxRequests(), properties.getWindowSeconds(), clientIp);
    }  // 行注：结束当前代码块

    /** 限制同一 IP 拉取验证码图片/题面的频率 */
    // 行注：定义检查验证码申请Rate限制方法
    public void checkCaptchaIssueRateLimit(HttpServletRequest request) {
        LinkxSecurityProperties.AuthRateLimit properties = linkxSecurityProperties.getAuthRateLimit();  // 行注：执行初始化操作
        // 行注：判断是否满足当前条件
        if (!properties.isEnabled()) {
            return;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        String clientIp = resolveClientIp(request);  // 行注：初始化客户端IP
        long maxRequests = Math.max(properties.getCaptchaIssueMaxRequests(), 1);  // 行注：初始化最大请求
        ensureWithinLimit("captcha-issue", clientIp, maxRequests, properties.getWindowSeconds(), clientIp);  // 行注：调用获取窗口Seconds
    }  // 行注：结束当前代码块

    /** 找回密码申请/确认按 IP + 标识限流 */
    public void checkPasswordResetRateLimit(HttpServletRequest request, String bucketPart) {
        LinkxSecurityProperties.AuthRateLimit properties = linkxSecurityProperties.getAuthRateLimit();
        if (!properties.isEnabled()) {
            return;
        }
        String clientIp = resolveClientIp(request);
        String key = normalizeKeyPart(bucketPart);
        long max = Math.max(properties.getLoginMaxRequests(), 5);
        ensureWithinLimit("password-reset", clientIp + ":" + key, max, properties.getWindowSeconds(), clientIp);
    }

    /**
     * 解析用于限流与审计的客户端 IP。
     * 未开启可信代理时仅使用 {@link HttpServletRequest#getRemoteAddr()}。
     */
    // 行注：定义解析客户端IP方法
    public String resolveClientIp(HttpServletRequest request) {
        // 行注：判断是否满足当前条件
        if (request == null) {
            return UNKNOWN_CLIENT_IP;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        LinkxSecurityProperties.TrustedProxy trustedProxy = linkxSecurityProperties.getTrustedProxy();  // 行注：执行初始化操作
        // 行注：判断是否满足当前条件
        if (trustedProxy.isEnabled()) {
            String forwardedFor = firstHeaderValue(request, "X-Forwarded-For");  // 行注：初始化forwarded
            // 行注：判断是否满足当前条件
            if (StringUtils.hasText(forwardedFor)) {
                return forwardedFor;  // 行注：返回处理结果
            }  // 行注：结束当前代码块
            String realIp = firstHeaderValue(request, "X-Real-IP");  // 行注：初始化realIP
            // 行注：判断是否满足当前条件
            if (StringUtils.hasText(realIp)) {
                return realIp;  // 行注：返回处理结果
            }  // 行注：结束当前代码块
        }  // 行注：结束当前代码块
        String remoteAddr = request.getRemoteAddr();  // 行注：初始化remoteAddr
        return StringUtils.hasText(remoteAddr) ? remoteAddr.trim() : UNKNOWN_CLIENT_IP;  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /** 用户端登录/注册页：是否展示验证码 */
    public CaptchaMetaDTO getCaptchaMeta() {
        return new CaptchaMetaDTO(isUserCaptchaEnabled(),
                List.of("login", "register", "admin-login"));
    }

    /** 管理端登录页：与 {@link #validateAdminLoginCaptcha} 使用同一开关（仅 YAML linkx.security.captcha） */
    public CaptchaMetaDTO getAdminCaptchaMeta() {
        boolean enabled = linkxSecurityProperties.getCaptcha() != null
                && linkxSecurityProperties.getCaptcha().isEnabled();
        return new CaptchaMetaDTO(enabled, List.of("admin-login"));
    }

    private boolean isUserCaptchaEnabled() {
        boolean yaml = linkxSecurityProperties.getCaptcha().isEnabled();
        return runtimeConfigService.getBoolean(RuntimeConfigService.KEY_USER_CAPTCHA_ENABLED, yaml);
    }

    /** 开启验证码时调用 {@link CaptchaService#consume} 一次性校验 */
    // 行注：定义validate验证码方法
    private void validateCaptcha(String scene, String captchaId, String captchaCode) {
        if ("admin-login".equals(scene)) {
            if (!linkxSecurityProperties.getCaptcha().isEnabled()) {
                return;
            }
        } else if (!isUserCaptchaEnabled()) {
            return;
        }
        captchaService.consume(scene, captchaId, captchaCode);  // 行注：调用consume
    }  // 行注：结束当前代码块

    /** Redis INCR + 首次设置 TTL 实现固定窗口计数，超限抛 {@link BusinessException} */
    // 行注：定义ensureWithin限制方法
    private void ensureWithinLimit(String scene, String bucketKey, long maxRequests, long windowSeconds, String clientIp) {
        // 行注：判断是否满足当前条件
        if (maxRequests <= 0 || windowSeconds <= 0) {
            return;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        String redisKey = RATE_LIMIT_KEY_PREFIX + scene + ":" + bucketKey;  // 行注：初始化Redis键
        Long currentCount = redisTemplate.opsForValue().increment(redisKey);  // 行注：初始化当前数量
        // 行注：判断是否满足当前条件
        if (currentCount != null && currentCount == 1L) {
            redisTemplate.expire(redisKey, Duration.ofSeconds(windowSeconds));  // 行注：调用expire
        }  // 行注：结束当前代码块
        // 行注：判断是否满足当前条件
        if (currentCount != null && currentCount > maxRequests) {
            // 行注：补充当前表达式片段
            log.warn("Auth rate limit blocked, scene={}, clientIp={}, bucketKey={}, currentCount={}, maxRequests={}",
                    scene, clientIp, bucketKey, currentCount, maxRequests);  // 行注：完成当前语句
            throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS, "请求过于频繁，请稍后再试");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
    }  // 行注：结束当前代码块

    // 行注：定义规范化键Part方法
    private String normalizeKeyPart(String value) {
        // 行注：判断是否满足当前条件
        if (!StringUtils.hasText(value)) {
            return "anonymous";  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        return value.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9._-]", "_");  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义firstHeader值方法
    private String firstHeaderValue(HttpServletRequest request, String headerName) {
        String headerValue = request.getHeader(headerName);  // 行注：初始化header值
        // 行注：判断是否满足当前条件
        if (!StringUtils.hasText(headerValue)) {
            return null;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        int separatorIndex = headerValue.indexOf(',');  // 行注：初始化separatorIndex
        String candidate = separatorIndex >= 0 ? headerValue.substring(0, separatorIndex) : headerValue;  // 行注：执行初始化操作
        String normalized = candidate.trim();  // 行注：初始化规范化后的
        return StringUtils.hasText(normalized) ? normalized : null;  // 行注：返回处理结果
    }  // 行注：结束当前代码块
}  // 行注：结束当前代码块
