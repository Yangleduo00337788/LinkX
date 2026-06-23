package com.linkx.server.module.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 验证码签发结果：前端展示 {@code imageDataUrl}，提交时带回 {@code captchaId} 与用户输入。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CaptchaIssueDTO {
    private String captchaId;
    /** SVG 内嵌 Data URL */
    private String imageDataUrl;
    private long expiresInSeconds;
}