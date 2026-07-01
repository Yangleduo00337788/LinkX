package com.linkx.server.module.auth.dto;  // 行注：声明当前文件所在包 com.linkx.server.module.auth.dto

import lombok.Builder;  // 行注：引入 Builder 类型
import lombok.Data;  // 行注：引入 Data 类型

/**
 * 注册/登录/刷新成功后的令牌与用户摘要。
 */
@Data  // 行注：应用 @Data 注解
@Builder  // 行注：应用 @Builder 注解
// 行注：定义 AuthResponse 类
public class AuthResponse {
    /** API 鉴权 Bearer access */
    private String accessToken;  // 行注：声明访问令牌字段
    /** 仅用于 /api/auth/refresh */
    private String refreshToken;  // 行注：声明刷新令牌字段
    /** 用户 ID */
    private Long userId;  // 行注：声明用户ID字段
    /** 用户名 */
    private String username;  // 行注：声明username字段
    /** 昵称 */
    private String nickname;  // 行注：声明nickname字段
    /** 头像 URL（可能为 ticket 化前的存储路径，前端再换 access-url） */
    private String avatar;
    /** 当前 refresh 绑定的设备会话 ID（多设备管理） */
    private String sessionId;
}
