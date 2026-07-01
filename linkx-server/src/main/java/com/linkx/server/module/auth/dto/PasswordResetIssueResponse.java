package com.linkx.server.module.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetIssueResponse {
    private String message;
    /** 仅开发/联调：无邮件通道时返回一次性 token */
    private String resetToken;
}