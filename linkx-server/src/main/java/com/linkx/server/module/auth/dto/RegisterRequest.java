package com.linkx.server.module.auth.dto;  // 行注：声明当前文件所在包 com.linkx.server.module.auth.dto

import jakarta.validation.constraints.NotBlank;  // 行注：引入 NotBlank 类型
import jakarta.validation.constraints.Size;  // 行注：引入 Size 类型
import lombok.Data;  // 行注：引入 Data 类型

/**
 * 开放注册请求体；密码强度另由 {@link com.linkx.server.module.auth.service.PasswordPolicy} 校验。
 */
@Data  // 行注：应用 @Data 注解
// 行注：定义 RegisterRequest 类
public class RegisterRequest {

    /** 用户名 */
    @NotBlank(message = "用户名不能为空")  // 行注：应用 @NotBlank 注解
    @Size(min = 3, max = 50, message = "用户名长度3-50位")  // 行注：应用 @Size 注解
    private String username;  // 行注：声明username字段

    /** 昵称 */
    @NotBlank(message = "昵称不能为空")  // 行注：应用 @NotBlank 注解
    @Size(min = 1, max = 50, message = "昵称长度1-50位")  // 行注：应用 @Size 注解
    private String nickname;  // 行注：声明nickname字段

    /** 密码哈希 */
    @NotBlank(message = "密码不能为空")  // 行注：应用 @NotBlank 注解
    @Size(min = 8, max = 50, message = "密码长度8-50位")  // 行注：应用 @Size 注解
    private String password;  // 行注：声明密码字段

    /** 验证码 ID */
    @Size(max = 128, message = "验证码ID长度不能超过128位")  // 行注：应用 @Size 注解
    private String captchaId;  // 行注：声明验证码ID字段

    /** 验证码答案 */
    @Size(max = 32, message = "验证码长度不能超过32位")  // 行注：应用 @Size 注解
    private String captchaCode;  // 行注：声明验证码验证码字段

    /** 可选，唯一性由数据库约束保证 */
    private String phone;  // 行注：声明phone字段

    /** 可选，唯一性由数据库约束保证 */
    private String email;  // 行注：声明email字段
}  // 行注：结束当前代码块
