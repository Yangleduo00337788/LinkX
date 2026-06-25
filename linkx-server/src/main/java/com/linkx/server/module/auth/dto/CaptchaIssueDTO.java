package com.linkx.server.module.auth.dto;  // 行注：声明当前文件所在包 com.linkx.server.module.auth.dto

import lombok.AllArgsConstructor;  // 行注：引入 AllArgsConstructor 类型
import lombok.Data;  // 行注：引入 Data 类型
import lombok.NoArgsConstructor;  // 行注：引入 NoArgsConstructor 类型

/**
 * 验证码签发结果：前端展示 {@code imageDataUrl}，提交时带回 {@code captchaId} 与用户输入。
 */
@Data  // 行注：应用 @Data 注解
@NoArgsConstructor  // 行注：应用 @NoArgsConstructor 注解
@AllArgsConstructor  // 行注：应用 @AllArgsConstructor 注解
// 行注：定义 CaptchaIssueDTO 类
public class CaptchaIssueDTO {
    /** 验证码 ID */
    private String captchaId;  // 行注：声明验证码ID字段
    /** SVG 内嵌 Data URL */
    private String imageDataUrl;  // 行注：声明imageDataURL字段
    /** 有效期（秒） */
    private long expiresInSeconds;  // 行注：声明expiresInSeconds字段
}  // 行注：结束当前代码块
