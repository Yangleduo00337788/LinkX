package com.linkx.server.module.auth.controller;

import com.linkx.server.common.Result;
import com.linkx.server.module.auth.dto.AuthResponse;
import com.linkx.server.module.auth.dto.CaptchaIssueDTO;
import com.linkx.server.module.auth.dto.CaptchaMetaDTO;
import com.linkx.server.module.auth.service.CaptchaService;
import com.linkx.server.module.auth.dto.LoginRequest;
import com.linkx.server.module.auth.dto.LogoutRequest;
import com.linkx.server.module.auth.dto.RefreshTokenRequest;
import com.linkx.server.module.auth.dto.RegisterRequest;
import com.linkx.server.module.auth.service.AuthService;
import com.linkx.server.module.auth.service.AuthSecurityGuard;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 认证公开接口：注册、登录、刷新、登出、验证码。
 * <p>
 * 均走 {@link AuthSecurityGuard} 限流与验证码；成功/登出写审计日志（含 clientIp）。
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AuthSecurityGuard authSecurityGuard;
    private final CaptchaService captchaService;

    @GetMapping("/captcha/meta")
    public Result<CaptchaMetaDTO> getCaptchaMeta() {
        return Result.success(authSecurityGuard.getCaptchaMeta());
    }

    @GetMapping("/captcha")
    public Result<CaptchaIssueDTO> issueCaptcha(
            @RequestParam(value = "scene", defaultValue = "login") String scene,
            HttpServletRequest request) {
        authSecurityGuard.checkCaptchaIssueRateLimit(request);
        return Result.success(captchaService.issue(scene));
    }

    @PostMapping("/register")
    public Result<AuthResponse> register(@Valid @RequestBody RegisterRequest requestBody, HttpServletRequest request) {
        authSecurityGuard.checkRegisterRateLimit(request, requestBody.getUsername());
        authSecurityGuard.validateRegisterCaptcha(requestBody.getCaptchaId(), requestBody.getCaptchaCode());
        AuthResponse response = authService.register(requestBody);
        log.info("Auth register success, userId={}, username={}, clientIp={}",
                response.getUserId(), response.getUsername(), authSecurityGuard.resolveClientIp(request));
        return Result.success(response);
    }

    @PostMapping("/login")
    public Result<AuthResponse> login(@Valid @RequestBody LoginRequest requestBody, HttpServletRequest request) {
        authSecurityGuard.checkLoginRateLimit(request, requestBody.getUsername());
        authSecurityGuard.validateLoginCaptcha(requestBody.getCaptchaId(), requestBody.getCaptchaCode());
        AuthResponse response = authService.login(requestBody);
        log.info("Auth login success, userId={}, username={}, clientIp={}",
                response.getUserId(), response.getUsername(), authSecurityGuard.resolveClientIp(request));
        return Result.success(response);
    }

    @PostMapping("/refresh")
    public Result<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest requestBody, HttpServletRequest request) {
        authSecurityGuard.checkRefreshRateLimit(request);
        AuthResponse response = authService.refreshToken(requestBody.getRefreshToken());
        log.info("Auth refresh success, userId={}, username={}, clientIp={}",
                response.getUserId(), response.getUsername(), authSecurityGuard.resolveClientIp(request));
        return Result.success(response);
    }

    @PostMapping("/logout")
    public Result<Void> logout(
            @RequestBody(required = false) LogoutRequest requestBody,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {
        Long userId = null;
        if (userDetails != null && userDetails.getUsername() != null) {
            try {
                userId = Long.parseLong(userDetails.getUsername());
            } catch (NumberFormatException ignored) {
                // ignore
            }
        }
        String refreshToken = requestBody != null ? requestBody.getRefreshToken() : null;
        String accessToken = requestBody != null ? requestBody.getAccessToken() : null;
        if (!StringUtils.hasText(accessToken)) {
            accessToken = extractBearerToken(request);
        }
        authService.logout(userId, refreshToken, accessToken);
        log.info("Auth logout success, userId={}, clientIp={}", userId, authSecurityGuard.resolveClientIp(request));
        return Result.success();
    }

    private static String extractBearerToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}
