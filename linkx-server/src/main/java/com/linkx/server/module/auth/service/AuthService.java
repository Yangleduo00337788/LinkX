package com.linkx.server.module.auth.service;  // 行注：声明当前文件所在包 com.linkx.server.module.auth.service

import com.linkx.server.module.auth.dto.AuthResponse;
import com.linkx.server.module.auth.dto.AuthSessionDTO;
import com.linkx.server.module.auth.dto.LoginRequest;
import com.linkx.server.module.auth.dto.RegisterRequest;

import java.util.List;

/**
 * 用户认证核心业务：注册、登录、刷新令牌、登出。
 */
// 行注：定义 AuthService 接口
public interface AuthService {

    /** 创建用户并签发 access + refresh */
    AuthResponse register(RegisterRequest request);  // 行注：调用注册

    /** 校验密码后签发令牌；失败统一 {@link com.linkx.server.common.ErrorCode#AUTH_LOGIN_FAILED} */
    AuthResponse login(LoginRequest request);

    AuthResponse login(LoginRequest request, String clientIp, String userAgent);

    /** 轮换 refresh，并签发新的 access + refresh */
    AuthResponse refreshToken(String refreshToken);  // 行注：调用刷新令牌

    /**
     * 撤销 refresh 会话并将 access 加入黑名单（若提供且有效）。
     *
     * @param userId       当前登录用户 ID，可为 null
     * @param refreshToken 请求体中的 refresh
     * @param accessToken  请求体或 Authorization 头中的 access
     */
    void logout(Long userId, String refreshToken, String accessToken);  // 行注：调用登出

    void changePassword(Long userId, String currentPassword, String newPassword);

    AuthResponse register(RegisterRequest request, String clientIp, String userAgent);

    List<AuthSessionDTO> listSessions(Long userId, String currentSessionId);

    void revokeSession(Long userId, String sessionId, String currentSessionId);

    void revokeOtherSessions(Long userId, String currentSessionId);

    /** 申请找回密码（用户名 + 已绑定邮箱，返回一次性 token；开发环境可在响应中返回） */
    String requestPasswordReset(String username, String email);

    void resetPasswordWithToken(String resetToken, String newPassword);
}
