package com.linkx.server.module.auth.controller;

import com.linkx.server.common.Result;
import com.linkx.server.module.auth.dto.PasswordResetApplyResponse;
import com.linkx.server.module.auth.dto.PasswordResetConfirmDTO;
import com.linkx.server.module.auth.dto.PasswordResetRequestDTO;
import com.linkx.server.module.auth.service.AuthSecurityGuard;
import com.linkx.server.module.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/password-reset")
@RequiredArgsConstructor
public class PasswordResetController {

    private final AuthService authService;
    private final AuthSecurityGuard authSecurityGuard;

    @Value("${linkx.auth.expose-reset-token:false}")
    private boolean exposeResetToken;

    @PostMapping("/request")
    public Result<PasswordResetApplyResponse> request(
            @Valid @RequestBody PasswordResetRequestDTO body,
            HttpServletRequest request) {
        authSecurityGuard.checkPasswordResetRateLimit(request, body.getUsername());
        String token = authService.requestPasswordReset(body.getUsername(), body.getEmail());
        PasswordResetApplyResponse.PasswordResetApplyResponseBuilder builder = PasswordResetApplyResponse.builder()
                .message("若账号与邮箱匹配，重置指引已处理；请查收邮件或使用开发环境返回的令牌");
        if (exposeResetToken) {
            builder.resetToken(token);
        }
        return Result.success(builder.build());
    }

    @PostMapping("/confirm")
    public Result<Void> confirm(
            @Valid @RequestBody PasswordResetConfirmDTO body,
            HttpServletRequest request) {
        authSecurityGuard.checkPasswordResetRateLimit(request, body.getResetToken());
        authService.resetPasswordWithToken(body.getResetToken(), body.getNewPassword());
        return Result.success();
    }
}