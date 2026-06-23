package com.linkx.server.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 安全相关配置，绑定 {@code linkx.security.*}（含环境变量覆盖）。
 * <p>
 * 用于 CORS、认证限流、验证码开关、可信代理、滥用防护（搜索/加好友）等。
 * </p>
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "linkx.security")
public class LinkxSecurityProperties {

    /** 允许的前端 Origin 模式列表，prod 必填且禁止 {@code *} */
    private List<String> allowedOriginPatterns = new ArrayList<>();
    private final AuthRateLimit authRateLimit = new AuthRateLimit();
    private final Captcha captcha = new Captcha();
    private final TrustedProxy trustedProxy = new TrustedProxy();
    private final AbuseProtection abuseProtection = new AbuseProtection();

    /** 过滤空串并 trim，供 CORS 使用 */
    public List<String> getAllowedOriginPatterns() {
        return allowedOriginPatterns.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .collect(Collectors.toList());
    }

    /** 登录/注册/刷新/验证码签发等场景的 Redis 滑动窗口限流 */
    @Getter
    @Setter
    public static class AuthRateLimit {
        private boolean enabled = true;
        /** 计数窗口秒数 */
        private long windowSeconds = 60;
        private long loginMaxRequests = 10;
        private long registerMaxRequests = 5;
        private long refreshMaxRequests = 20;
        private long captchaIssueMaxRequests = 30;
    }

    @Getter
    @Setter
    public static class Captcha {
        /** 生产环境启动校验要求为 true */
        private boolean enabled = false;
    }

    @Getter
    @Setter
    public static class TrustedProxy {
        /** 为 true 时从 X-Forwarded-For 等头解析客户端 IP（仅信任前置反代） */
        private boolean enabled = false;
    }

    /** 公网开放：用户搜索与好友申请频率控制 */
    @Getter
    @Setter
    public static class AbuseProtection {
        private boolean enabled = true;
        private final UserSearch userSearch = new UserSearch();
        private final FriendRequest friendRequest = new FriendRequest();

        @Getter
        @Setter
        public static class UserSearch {
            private long windowSeconds = 60;
            private long maxRequestsPerUser = 30;
            private long maxRequestsPerIp = 60;
        }

        @Getter
        @Setter
        public static class FriendRequest {
            /** 自然日按服务器本地时区（如 Asia/Shanghai） */
            private long maxPerUserPerDay = 20;
            private long maxPerIpPerDay = 50;
        }
    }
}