package com.linkx.server.module.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 开放注册请求体；密码强度另由 {@link com.linkx.server.module.auth.service.PasswordPolicy} 校验。
 */
@Data
public class RegisterRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度3-50位")
    private String username;

    @NotBlank(message = "昵称不能为空")
    @Size(min = 1, max = 50, message = "昵称长度1-50位")
    private String nickname;

    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 50, message = "密码长度8-50位")
    private String password;

    @Size(max = 128, message = "验证码ID长度不能超过128位")
    private String captchaId;

    @Size(max = 32, message = "验证码长度不能超过32位")
    private String captchaCode;

    /** 可选，唯一性由数据库约束保证 */
    private String phone;

    /** 可选，唯一性由数据库约束保证 */
    private String email;
}