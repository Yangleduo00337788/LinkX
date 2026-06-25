package com.linkx.server.module.auth.dto;  // 行注：声明当前文件所在包 com.linkx.server.module.auth.dto

import jakarta.validation.constraints.NotBlank;  // 行注：引入 NotBlank 类型
import jakarta.validation.constraints.Size;  // 行注：引入 Size 类型
import lombok.Data;  // 行注：引入 Data 类型

/**
 * 登录请求体。
 * <p>生产环境开启验证码时须携带 {@code captchaId}、{@code captchaCode}。</p>
 */
@Data  // 行注：应用 @Data 注解
// 行注：定义 LoginRequest 类
public class LoginRequest {

    /** 登录用户名 */
    @NotBlank(message = "用户名不能为空")  // 行注：应用 @NotBlank 注解
    private String username;  // 行注：声明username字段

    /** 明文密码（HTTPS 传输，服务端 BCrypt 校验） */
    @NotBlank(message = "密码不能为空")  // 行注：应用 @NotBlank 注解
    private String password;  // 行注：声明密码字段

    /** 验证码会话 ID，来自 GET /api/auth/captcha */
    @Size(max = 128, message = "验证码ID长度不能超过128位")  // 行注：应用 @Size 注解
    private String captchaId;  // 行注：声明验证码ID字段

    /** 验证码答案 */
    @Size(max = 32, message = "验证码长度不能超过32位")  // 行注：应用 @Size 注解
    private String captchaCode;  // 行注：声明验证码验证码字段
}  // 行注：结束当前代码块
