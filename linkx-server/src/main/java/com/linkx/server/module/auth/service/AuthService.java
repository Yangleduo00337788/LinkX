package com.linkx.server.module.auth.service;

import com.linkx.server.module.auth.dto.AuthResponse;
import com.linkx.server.module.auth.dto.LoginRequest;
import com.linkx.server.module.auth.dto.RegisterRequest;

/**
 * 用户认证核心业务：注册、登录、刷新令牌、登出。
 */
public interface AuthService {

    /** 创建用户并签发 access + refresh */
    AuthResponse register(RegisterRequest request);

    /** 校验密码后签发令牌；失败统一 {@link com.linkx.server.common.ErrorCode#AUTH_LOGIN_FAILED} */
    AuthResponse login(LoginRequest request);

    /** 轮换 refresh，并签发新的 access + refresh */
    AuthResponse refreshToken(String refreshToken);

    /**
     * 撤销 refresh 会话并将 access 加入黑名单（若提供且有效）。
     *
     * @param userId       当前登录用户 ID，可为 null
     * @param refreshToken 请求体中的 refresh
     * @param accessToken  请求体或 Authorization 头中的 access
     */
    void logout(Long userId, String refreshToken, String accessToken);
}