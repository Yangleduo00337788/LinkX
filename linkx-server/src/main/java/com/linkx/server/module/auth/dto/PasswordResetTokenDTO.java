package com.linkx.server.module.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PasswordResetTokenDTO {
    /** 开发/未接邮件时前端可展示；生产应仅发邮件 */
    private String resetToken;
    private String message;
}