package com.linkx.server.module.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminLoginRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    /** 验证码 ID（开启验证码时必填） */
    private String captchaId;

    /** 验证码内容 */
    private String captchaCode;
}