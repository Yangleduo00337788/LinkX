package com.linkx.server.module.auth.dto;

import lombok.Data;

/**
 * 登出请求体（均可选）；未传 access 时服务端从 Authorization 头读取。
 */
@Data
public class LogoutRequest {

    /** 撤销 Redis 中的 refresh 会话 */
    private String refreshToken;
    /** 加入 access 黑名单直至过期 */
    private String accessToken;
}