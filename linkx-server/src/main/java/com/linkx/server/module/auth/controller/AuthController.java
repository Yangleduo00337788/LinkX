package com.linkx.server.module.auth.controller;  // 行注：声明当前文件所在包 com.linkx.server.module.auth.controller

import com.linkx.server.common.Result;  // 行注：引入 Result 类型
import com.linkx.server.module.auth.dto.AuthResponse;  // 行注：引入 AuthResponse 类型
import com.linkx.server.module.auth.dto.CaptchaIssueDTO;  // 行注：引入 CaptchaIssueDTO 类型
import com.linkx.server.module.auth.dto.CaptchaMetaDTO;  // 行注：引入 CaptchaMetaDTO 类型
import com.linkx.server.module.auth.service.CaptchaService;  // 行注：引入 CaptchaService 类型
import com.linkx.server.module.auth.dto.LoginRequest;  // 行注：引入 LoginRequest 类型
import com.linkx.server.module.auth.dto.LogoutRequest;  // 行注：引入 LogoutRequest 类型
import com.linkx.server.module.auth.dto.RefreshTokenRequest;  // 行注：引入 RefreshTokenRequest 类型
import com.linkx.server.module.auth.dto.RegisterRequest;  // 行注：引入 RegisterRequest 类型
import com.linkx.server.module.auth.service.AuthService;  // 行注：引入 AuthService 类型
import com.linkx.server.module.auth.service.AuthSecurityGuard;  // 行注：引入 AuthSecurityGuard 类型
import jakarta.servlet.http.HttpServletRequest;  // 行注：引入 HttpServletRequest 类型
import jakarta.validation.Valid;  // 行注：引入 Valid 类型
import lombok.RequiredArgsConstructor;  // 行注：引入 RequiredArgsConstructor 类型
import lombok.extern.slf4j.Slf4j;  // 行注：引入 Slf4j 类型
import org.springframework.security.core.annotation.AuthenticationPrincipal;  // 行注：引入 AuthenticationPrincipal 类型
import org.springframework.security.core.userdetails.UserDetails;  // 行注：引入 UserDetails 类型
import org.springframework.util.StringUtils;  // 行注：引入 StringUtils 类型
import org.springframework.web.bind.annotation.*;  // 行注：引入 * 类型

/**
 * 认证公开接口：注册、登录、刷新、登出、验证码。
 * <p>
 * 均走 {@link AuthSecurityGuard} 限流与验证码；成功/登出写审计日志（含 clientIp）。
 * </p>
 */
@Slf4j  // 行注：应用 @Slf4j 注解
@RestController  // 行注：应用 @RestController 注解
@RequestMapping("/api/auth")  // 行注：应用 @RequestMapping 注解
@RequiredArgsConstructor  // 行注：应用 @RequiredArgsConstructor 注解
// 行注：定义 AuthController 类
public class AuthController {

    private final AuthService authService;  // 行注：注入认证服务依赖
    private final AuthSecurityGuard authSecurityGuard;  // 行注：注入认证安全Guard依赖
    private final CaptchaService captchaService;  // 行注：注入验证码服务依赖

    /** 查询验证码是否启用及前端展示所需元数据（无需登录） */
    @GetMapping("/captcha/meta")  // 行注：应用 @GetMapping 注解
    // 行注：定义获取验证码元数据方法
    public Result<CaptchaMetaDTO> getCaptchaMeta() {
        return Result.success(authSecurityGuard.getCaptchaMeta());  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /**
     * 签发验证码（图片或滑块等由 {@link CaptchaService} 实现决定）。
     *
     * @param scene 业务场景，默认 login，也可用于 register
     */
    @GetMapping("/captcha")  // 行注：应用 @GetMapping 注解
    // 行注：定义申请验证码方法
    public Result<CaptchaIssueDTO> issueCaptcha(
            @RequestParam(value = "scene", defaultValue = "login") String scene,  // 行注：应用 @RequestParam 注解
            // 行注：开始当前语句对应的代码块
            HttpServletRequest request) {
        authSecurityGuard.checkCaptchaIssueRateLimit(request);  // 行注：调用检查验证码申请Rate限制
        return Result.success(captchaService.issue(scene));  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /** 注册新用户：限流 → 验证码 → 创建账号并返回令牌对 */
    @PostMapping("/register")  // 行注：应用 @PostMapping 注解
    // 行注：定义注册方法
    public Result<AuthResponse> register(@Valid @RequestBody RegisterRequest requestBody, HttpServletRequest request) {
        authSecurityGuard.checkRegisterRateLimit(request, requestBody.getUsername());  // 行注：调用检查注册Rate限制
        authSecurityGuard.validateRegisterCaptcha(requestBody.getCaptchaId(), requestBody.getCaptchaCode());  // 行注：调用validate注册验证码
        AuthResponse response = authService.register(requestBody);  // 行注：初始化response
        // 行注：补充当前表达式片段
        log.info("Auth register success, userId={}, username={}, clientIp={}",
                response.getUserId(), response.getUsername(), authSecurityGuard.resolveClientIp(request));  // 行注：调用获取用户ID
        return Result.success(response);  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /** 用户名密码登录：限流 → 验证码 → 校验凭证并返回令牌对 */
    @PostMapping("/login")  // 行注：应用 @PostMapping 注解
    // 行注：定义登录方法
    public Result<AuthResponse> login(@Valid @RequestBody LoginRequest requestBody, HttpServletRequest request) {
        authSecurityGuard.checkLoginRateLimit(request, requestBody.getUsername());  // 行注：调用检查登录Rate限制
        authSecurityGuard.validateLoginCaptcha(requestBody.getCaptchaId(), requestBody.getCaptchaCode());  // 行注：调用validate登录验证码
        AuthResponse response = authService.login(requestBody);  // 行注：初始化response
        // 行注：补充当前表达式片段
        log.info("Auth login success, userId={}, username={}, clientIp={}",
                response.getUserId(), response.getUsername(), authSecurityGuard.resolveClientIp(request));  // 行注：调用获取用户ID
        return Result.success(response);  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /** 使用 refresh token 轮换会话并返回新的 access + refresh */
    @PostMapping("/refresh")  // 行注：应用 @PostMapping 注解
    // 行注：定义刷新令牌方法
    public Result<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest requestBody, HttpServletRequest request) {
        authSecurityGuard.checkRefreshRateLimit(request);  // 行注：调用检查刷新Rate限制
        AuthResponse response = authService.refreshToken(requestBody.getRefreshToken());  // 行注：初始化response
        // 行注：补充当前表达式片段
        log.info("Auth refresh success, userId={}, username={}, clientIp={}",
                response.getUserId(), response.getUsername(), authSecurityGuard.resolveClientIp(request));  // 行注：调用获取用户ID
        return Result.success(response);  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /**
     * 登出：撤销 refresh、将 access 加入黑名单（若提供）。
     * 用户 ID 来自 Security 上下文；access 可从 body 或 Authorization 头读取。
     */
    @PostMapping("/logout")  // 行注：应用 @PostMapping 注解
    // 行注：定义登出方法
    public Result<Void> logout(
            @RequestBody(required = false) LogoutRequest requestBody,  // 行注：应用 @RequestBody 注解
            @AuthenticationPrincipal UserDetails userDetails,  // 行注：应用 @AuthenticationPrincipal UserDetails userDetails, 注解
            // 行注：开始当前语句对应的代码块
            HttpServletRequest request) {
        Long userId = null;  // 行注：初始化用户ID
        // 行注：判断是否满足当前条件
        if (userDetails != null && userDetails.getUsername() != null) {
            // 行注：尝试执行可能失败的逻辑
            try {
                userId = Long.parseLong(userDetails.getUsername());  // 行注：初始化用户ID
            // 行注：执行当前方法调用
            } catch (NumberFormatException ignored) {
                // UserDetails 的 username 非数字时无法解析为 userId，登出仍可按 token 撤销
            }  // 行注：结束当前代码块
        }  // 行注：结束当前代码块
        String refreshToken = requestBody != null ? requestBody.getRefreshToken() : null;  // 行注：执行初始化操作
        String accessToken = requestBody != null ? requestBody.getAccessToken() : null;  // 行注：执行初始化操作
        // 行注：判断是否满足当前条件
        if (!StringUtils.hasText(accessToken)) {
            accessToken = extractBearerToken(request);  // 行注：初始化访问令牌
        }  // 行注：结束当前代码块
        authService.logout(userId, refreshToken, accessToken);  // 行注：调用登出
        log.info("Auth logout success, userId={}, clientIp={}", userId, authSecurityGuard.resolveClientIp(request));  // 行注：执行初始化操作
        return Result.success();  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /**
     * 从标准 Authorization 头解析 Bearer access token。
     *
     * @param request 当前 HTTP 请求
     * @return token 字符串；无头或非 Bearer 时返回 null
     */
    // 行注：定义extractBearer令牌方法
    private static String extractBearerToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");  // 行注：初始化bearer
        // 行注：判断是否满足当前条件
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        return null;  // 行注：返回处理结果
    }  // 行注：结束当前代码块
}  // 行注：结束当前代码块
