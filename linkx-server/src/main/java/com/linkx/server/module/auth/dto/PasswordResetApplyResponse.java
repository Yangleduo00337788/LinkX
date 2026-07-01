package com.linkx.server.module.auth.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 申请找回密码响应。生产环境应通过邮件发送 token，不返回 resetToken。
 */
@Data
@Builder
public class PasswordResetApplyResponse {
    private String message;
    /** 仅 dev 或未配置邮件时返回，便于联调 */
    private String resetToken;
}