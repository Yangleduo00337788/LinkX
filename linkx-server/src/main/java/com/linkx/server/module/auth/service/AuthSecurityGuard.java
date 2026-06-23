package com.linkx.server.module.auth.service;

import com.linkx.server.common.BusinessException;
import com.linkx.server.common.ErrorCode;
import com.linkx.server.config.LinkxSecurityProperties;
import com.linkx.server.module.auth.dto.CaptchaMetaDTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.List;
import java.util.Locale;

/**
 * 认证链路安全：验证码校验、Redis 滑动窗口限流、客户端 IP 解析。
 * <p>
 * IP 仅在 {@link LinkxSecurityProperties.TrustedProxy#enabled} 为 true 时信任 X-Forwarded-For。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthSecurityGuard {

    private static final String RATE_LIMIT_KEY_PREFIX = "auth:rate-limit:";
    private static final String UNKNOWN_CLIENT_IP = "unknown";

    private final RedisTemplate<String, Object> redisTemplate;
    private final LinkxSecurityProperties linkxSecurityProperties;
    private final CaptchaService captchaService;

    public void validateLoginCaptcha(String captchaId, String captchaCode) {
        validateCaptcha("login", captchaId, captchaCode);
    }

    public void validateRegisterCaptcha(String captchaId, String captchaCode) {
        validateCaptcha("register", captchaId, captchaCode);
    }

    public void checkLoginRateLimit(HttpServletRequest request, String username) {
        LinkxSecurityProperties.AuthRateLimit properties = linkxSecurityProperties.getAuthRateLimit();
        if (!properties.isEnabled()) {
            return;
        }
        String clientIp = resolveClientIp(request);
        String normalizedUsername = normalizeKeyPart(username);
        String bucketKey = clientIp + ":" + normalizedUsername;
        ensureWithinLimit("login", bucketKey, properties.getLoginMaxRequests(), properties.getWindowSeconds(), clientIp);
    }

    public void checkRegisterRateLimit(HttpServletRequest request, String username) {
        LinkxSecurityProperties.AuthRateLimit properties = linkxSecurityProperties.getAuthRateLimit();
        if (!properties.isEnabled()) {
            return;
        }
        String clientIp = resolveClientIp(request);
        String normalizedUsername = normalizeKeyPart(username);
        String bucketKey = clientIp + ":" + normalizedUsername;
        ensureWithinLimit("register", bucketKey, properties.getRegisterMaxRequests(), properties.getWindowSeconds(), clientIp);
    }

    public void checkRefreshRateLimit(HttpServletRequest request) {
        LinkxSecurityProperties.AuthRateLimit properties = linkxSecurityProperties.getAuthRateLimit();
        if (!properties.isEnabled()) {
            return;
        }
        String clientIp = resolveClientIp(request);
        ensureWithinLimit("refresh", clientIp, properties.getRefreshMaxRequests(), properties.getWindowSeconds(), clientIp);
    }

    public void checkCaptchaIssueRateLimit(HttpServletRequest request) {
        LinkxSecurityProperties.AuthRateLimit properties = linkxSecurityProperties.getAuthRateLimit();
        if (!properties.isEnabled()) {
            return;
        }
        String clientIp = resolveClientIp(request);
        long maxRequests = Math.max(properties.getCaptchaIssueMaxRequests(), 1);
        ensureWithinLimit("captcha-issue", clientIp, maxRequests, properties.getWindowSeconds(), clientIp);
    }

    public String resolveClientIp(HttpServletRequest request) {
        if (request == null) {
            return UNKNOWN_CLIENT_IP;
        }
        LinkxSecurityProperties.TrustedProxy trustedProxy = linkxSecurityProperties.getTrustedProxy();
        if (trustedProxy.isEnabled()) {
            String forwardedFor = firstHeaderValue(request, "X-Forwarded-For");
            if (StringUtils.hasText(forwardedFor)) {
                return forwardedFor;
            }
            String realIp = firstHeaderValue(request, "X-Real-IP");
            if (StringUtils.hasText(realIp)) {
                return realIp;
            }
        }
        String remoteAddr = request.getRemoteAddr();
        return StringUtils.hasText(remoteAddr) ? remoteAddr.trim() : UNKNOWN_CLIENT_IP;
    }

    public CaptchaMetaDTO getCaptchaMeta() {
        return new CaptchaMetaDTO(linkxSecurityProperties.getCaptcha().isEnabled(), List.of("login", "register"));
    }

    private void validateCaptcha(String scene, String captchaId, String captchaCode) {
        if (!linkxSecurityProperties.getCaptcha().isEnabled()) {
            return;
        }
        captchaService.consume(scene, captchaId, captchaCode);
    }

    private void ensureWithinLimit(String scene, String bucketKey, long maxRequests, long windowSeconds, String clientIp) {
        if (maxRequests <= 0 || windowSeconds <= 0) {
            return;
        }
        String redisKey = RATE_LIMIT_KEY_PREFIX + scene + ":" + bucketKey;
        Long currentCount = redisTemplate.opsForValue().increment(redisKey);
        if (currentCount != null && currentCount == 1L) {
            redisTemplate.expire(redisKey, Duration.ofSeconds(windowSeconds));
        }
        if (currentCount != null && currentCount > maxRequests) {
            log.warn("Auth rate limit blocked, scene={}, clientIp={}, bucketKey={}, currentCount={}, maxRequests={}",
                    scene, clientIp, bucketKey, currentCount, maxRequests);
            throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS, "请求过于频繁，请稍后再试");
        }
    }

    private String normalizeKeyPart(String value) {
        if (!StringUtils.hasText(value)) {
            return "anonymous";
        }
        return value.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9._-]", "_");
    }

    private String firstHeaderValue(HttpServletRequest request, String headerName) {
        String headerValue = request.getHeader(headerName);
        if (!StringUtils.hasText(headerValue)) {
            return null;
        }
        int separatorIndex = headerValue.indexOf(',');
        String candidate = separatorIndex >= 0 ? headerValue.substring(0, separatorIndex) : headerValue;
        String normalized = candidate.trim();
        return StringUtils.hasText(normalized) ? normalized : null;
    }
}