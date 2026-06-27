package com.linkx.server.module.admin.controller;

import com.linkx.server.common.Result;
import com.linkx.server.module.admin.dto.AdminAuthResponse;
import com.linkx.server.module.admin.dto.AdminLoginRequest;
import com.linkx.server.module.admin.dto.AdminProfileDTO;
import com.linkx.server.module.admin.service.AdminAuthService;
import com.linkx.server.module.auth.dto.CaptchaIssueDTO;
import com.linkx.server.module.auth.dto.CaptchaMetaDTO;
import com.linkx.server.module.auth.service.AuthSecurityGuard;
import com.linkx.server.module.auth.service.CaptchaService;
import com.linkx.server.security.AdminPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminAuthService adminAuthService;
    private final AuthSecurityGuard authSecurityGuard;
    private final CaptchaService captchaService;

    @GetMapping("/captcha/meta")
    public Result<CaptchaMetaDTO> captchaMeta() {
        return Result.success(authSecurityGuard.getCaptchaMeta());
    }

    @GetMapping("/captcha")
    public Result<CaptchaIssueDTO> issueCaptcha(HttpServletRequest request) {
        authSecurityGuard.checkCaptchaIssueRateLimit(request);
        return Result.success(captchaService.issue("admin-login"));
    }

    @PostMapping("/login")
    public Result<AdminAuthResponse> login(@Valid @RequestBody AdminLoginRequest body, HttpServletRequest request) {
        authSecurityGuard.validateAdminLoginCaptcha(body.getCaptchaId(), body.getCaptchaCode());
        String ip = authSecurityGuard.resolveClientIp(request);
        return Result.success(adminAuthService.login(body, ip));
    }

    @GetMapping("/profile")
    public Result<AdminProfileDTO> profile(@AuthenticationPrincipal AdminPrincipal principal) {
        return Result.success(adminAuthService.getProfile(principal.getAdminId()));
    }
}