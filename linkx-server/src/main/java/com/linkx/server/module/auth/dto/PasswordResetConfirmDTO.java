package com.linkx.server.module.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PasswordResetConfirmDTO {

    @NotBlank(message = "重置令牌不能为空")
    private String resetToken;

    @NotBlank(message = "新密码不能为空")
    private String newPassword;
}