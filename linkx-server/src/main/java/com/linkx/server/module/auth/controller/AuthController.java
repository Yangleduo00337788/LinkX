package com.linkx.server.module.auth.controller;

import com.linkx.server.common.Result;
import com.linkx.server.module.auth.dto.AuthResponse;
import com.linkx.server.module.auth.dto.CaptchaMetaDTO;
import com.linkx.server.module.auth.dto.LoginRequest;
import com.linkx.server.module.auth.dto.RefreshTokenRequest;
import com.linkx.server.module.auth.dto.RegisterRequest;
import com.linkx.server.module.auth.service.AuthService;
import com.linkx.server.module.auth.service.AuthSecurityGuard;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AuthSecurityGuard authSecurityGuard;

    @GetMapping("/captcha/meta")
    public Result<CaptchaMetaDTO> getCaptchaMeta() {
        return Result.success(authSecurityGuard.getCaptchaMeta());
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
}
