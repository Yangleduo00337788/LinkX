package com.linkx.server.config;  // 行注：声明当前文件所在包 com.linkx.server.config

import lombok.Getter;  // 行注：引入 Getter 类型
import lombok.Setter;  // 行注：引入 Setter 类型
import org.springframework.boot.context.properties.ConfigurationProperties;  // 行注：引入 ConfigurationProperties 类型
import org.springframework.stereotype.Component;  // 行注：引入 Component 类型
import org.springframework.util.StringUtils;  // 行注：引入 StringUtils 类型

import java.util.ArrayList;  // 行注：引入 ArrayList 类型
import java.util.List;  // 行注：引入 List 类型
import java.util.stream.Collectors;  // 行注：引入 Collectors 类型

/**
 * 安全相关配置，绑定 {@code linkx.security.*}（含环境变量覆盖）。
 * <p>
 * 用于 CORS、认证限流、验证码开关、可信代理、滥用防护（搜索/加好友）等。
 * </p>
 */
@Getter  // 行注：应用 @Getter 注解
@Setter  // 行注：应用 @Setter 注解
@Component  // 行注：应用 @Component 注解
@ConfigurationProperties(prefix = "linkx.security")  // 行注：应用 @ConfigurationProperties 注解
// 行注：定义 LinkxSecurityProperties 类
public class LinkxSecurityProperties {

    /** 允许的前端 Origin 模式列表，prod 必填且禁止 {@code *} */
    private List<String> allowedOriginPatterns = new ArrayList<>();  // 行注：定义当前方法
    /** 认证相关接口的 Redis 滑动窗口限流参数 */
    private final AuthRateLimit authRateLimit = new AuthRateLimit();  // 行注：注入认证Rate限制依赖
    /** 图形/滑块验证码开关（生产环境须开启） */
    private final Captcha captcha = new Captcha();  // 行注：注入验证码依赖
    /** 是否信任反向代理头解析真实客户端 IP */
    private final TrustedProxy trustedProxy = new TrustedProxy();  // 行注：注入trusted代理依赖
    /** 用户搜索、好友申请等滥用防护阈值 */
    private final AbuseProtection abuseProtection = new AbuseProtection();  // 行注：注入abuseProtection依赖

    /** 过滤空串并 trim，供 CORS 使用 */
    // 行注：定义获取允许来源Patterns方法
    public List<String> getAllowedOriginPatterns() {
        return allowedOriginPatterns.stream()  // 行注：返回处理结果
                // 行注：继续调用过滤
                .filter(StringUtils::hasText)
                // 行注：继续调用映射
                .map(String::trim)
                .collect(Collectors.toList());  // 行注：继续调用收集
    }  // 行注：结束当前代码块

    /** 登录/注册/刷新/验证码签发等场景的 Redis 滑动窗口限流 */
    @Getter  // 行注：应用 @Getter 注解
    @Setter  // 行注：应用 @Setter 注解
    // 行注：定义 AuthRateLimit 类
    public static class AuthRateLimit {
        private boolean enabled = true;  // 行注：声明启用字段
        /** 计数窗口秒数 */
        private long windowSeconds = 60;  // 行注：声明窗口Seconds字段
        private long loginMaxRequests = 10;  // 行注：声明登录最大请求字段
        private long registerMaxRequests = 5;  // 行注：声明注册最大请求字段
        private long refreshMaxRequests = 20;  // 行注：声明刷新最大请求字段
        private long captchaIssueMaxRequests = 30;  // 行注：声明验证码申请最大请求字段
    }  // 行注：结束当前代码块

    @Getter  // 行注：应用 @Getter 注解
    @Setter  // 行注：应用 @Setter 注解
    // 行注：定义 Captcha 类
    public static class Captcha {
        /** 生产环境启动校验要求为 true */
        private boolean enabled = false;  // 行注：声明启用字段
    }  // 行注：结束当前代码块

    @Getter  // 行注：应用 @Getter 注解
    @Setter  // 行注：应用 @Setter 注解
    // 行注：定义 TrustedProxy 类
    public static class TrustedProxy {
        /** 为 true 时从 X-Forwarded-For 等头解析客户端 IP（仅信任前置反代） */
        private boolean enabled = false;  // 行注：声明启用字段
    }  // 行注：结束当前代码块

    /** 公网开放：用户搜索与好友申请频率控制 */
    @Getter  // 行注：应用 @Getter 注解
    @Setter  // 行注：应用 @Setter 注解
    // 行注：定义 AbuseProtection 类
    public static class AbuseProtection {
        private boolean enabled = true;  // 行注：声明启用字段
        private final UserSearch userSearch = new UserSearch();  // 行注：注入用户搜索依赖
        private final FriendRequest friendRequest = new FriendRequest();  // 行注：注入好友请求依赖

        @Getter  // 行注：应用 @Getter 注解
        @Setter  // 行注：应用 @Setter 注解
        // 行注：定义 UserSearch 类
        public static class UserSearch {
            private long windowSeconds = 60;  // 行注：声明窗口Seconds字段
            private long maxRequestsPerUser = 30;  // 行注：声明最大请求Per用户字段
            private long maxRequestsPerIp = 60;  // 行注：声明最大请求PerIP字段
        }  // 行注：结束当前代码块

        @Getter  // 行注：应用 @Getter 注解
        @Setter  // 行注：应用 @Setter 注解
        // 行注：定义 FriendRequest 类
        public static class FriendRequest {
            /** 自然日按服务器本地时区（如 Asia/Shanghai） */
            private long maxPerUserPerDay = 20;  // 行注：声明最大Per用户Per日期字段
            private long maxPerIpPerDay = 50;  // 行注：声明最大PerIPPer日期字段
        }  // 行注：结束当前代码块
    }  // 行注：结束当前代码块
}  // 行注：结束当前代码块
