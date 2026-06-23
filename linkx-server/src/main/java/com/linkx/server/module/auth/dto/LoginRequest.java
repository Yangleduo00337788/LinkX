package com.linkx.server.module.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 登录请求体。
 * <p>生产环境开启验证码时须携带 {@code captchaId}、{@code captchaCode}。</p>
 */
@Data
public class LoginRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    /** 验证码会话 ID，来自 GET /api/auth/captcha */
    @Size(max = 128, message = "验证码ID长度不能超过128位")
    private String captchaId;

    @Size(max = 32, message = "验证码长度不能超过32位")
    private String captchaCode;
}