package com.linkx.server.module.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForgotPasswordRequest {
    @NotBlank
    private String username;
    @NotBlank
    @Email(message = "邮箱格式不正确")
    private String email;
}