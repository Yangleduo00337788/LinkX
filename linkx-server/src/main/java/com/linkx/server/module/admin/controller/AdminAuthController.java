package com.linkx.server.module.admin.controller;

import com.linkx.server.common.Result;
import com.linkx.server.module.admin.dto.AdminAuthResponse;
import com.linkx.server.module.admin.dto.AdminLoginRequest;
import com.linkx.server.module.admin.dto.AdminProfileDTO;
import com.linkx.server.module.admin.service.AdminAuthService;
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

    @PostMapping("/login")
    public Result<AdminAuthResponse> login(@Valid @RequestBody AdminLoginRequest body, HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        return Result.success(adminAuthService.login(body, ip));
    }

    @GetMapping("/profile")
    public Result<AdminProfileDTO> profile(@AuthenticationPrincipal AdminPrincipal principal) {
        return Result.success(adminAuthService.getProfile(principal.getAdminId()));
    }
}