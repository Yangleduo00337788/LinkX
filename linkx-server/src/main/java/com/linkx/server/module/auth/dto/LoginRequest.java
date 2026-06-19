package com.linkx.server.module.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    @Size(max = 128, message = "验证码ID长度不能超过128位")
    private String captchaId;

    @Size(max = 32, message = "验证码长度不能超过32位")
    private String captchaCode;
}
