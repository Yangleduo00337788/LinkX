package com.linkx.server.module.auth.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PasswordResetResponseDTO {
    /** 开发/联调环境可返回；生产应仅发邮件 */
    private String resetToken;
    private String message;
}