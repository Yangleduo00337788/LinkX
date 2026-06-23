package com.linkx.server.module.auth.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 注册/登录/刷新成功后的令牌与用户摘要。
 */
@Data
@Builder
public class AuthResponse {
    /** API 鉴权 Bearer access */
    private String accessToken;
    /** 仅用于 /api/auth/refresh */
    private String refreshToken;
    private Long userId;
    private String username;
    private String nickname;
    /** 头像 URL（可能为 ticket 化前的存储路径，前端再换 access-url） */
    private String avatar;
}