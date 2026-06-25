package com.linkx.server.module.auth.dto;  // 行注：声明当前文件所在包 com.linkx.server.module.auth.dto

import jakarta.validation.constraints.NotBlank;  // 行注：引入 NotBlank 类型
import lombok.Data;  // 行注：引入 Data 类型

/** 使用 refresh 轮换一对新 access + refresh。 */
@Data  // 行注：应用 @Data 注解
// 行注：定义 RefreshTokenRequest 类
public class RefreshTokenRequest {

    /** 刷新令牌 */
    @NotBlank(message = "刷新令牌不能为空")  // 行注：应用 @NotBlank 注解
    private String refreshToken;  // 行注：声明刷新令牌字段
}  // 行注：结束当前代码块
