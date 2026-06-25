package com.linkx.server.module.auth.dto;  // 行注：声明当前文件所在包 com.linkx.server.module.auth.dto

import lombok.Data;  // 行注：引入 Data 类型

/**
 * 登出请求体（均可选）；未传 access 时服务端从 Authorization 头读取。
 */
@Data  // 行注：应用 @Data 注解
// 行注：定义 LogoutRequest 类
public class LogoutRequest {

    /** 撤销 Redis 中的 refresh 会话 */
    private String refreshToken;  // 行注：声明刷新令牌字段
    /** 加入 access 黑名单直至过期 */
    private String accessToken;  // 行注：声明访问令牌字段
}  // 行注：结束当前代码块
