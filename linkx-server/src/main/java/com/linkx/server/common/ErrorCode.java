package com.linkx.server.common;

import lombok.Getter;

/**
 * 全局业务与 HTTP 语义错误码。
 * <p>
 * 4xx/5xx 段与 HTTP 习惯对齐；1xxx 用户域；2xxx 认证令牌域。
 * 登录失败对外统一使用 {@link #AUTH_LOGIN_FAILED}，避免区分「用户不存在」与「密码错误」。
 * </p>
 */
@Getter
public enum ErrorCode {
    /** 成功（一般通过 Result.code=200 表示，枚举内保留便于对照） */
    SUCCESS(200, "success"),
    /** 参数校验失败、非法业务参数 */
    BAD_REQUEST(400, "请求参数错误"),
    /** 未登录或令牌无效 */
    UNAUTHORIZED(401, "未认证"),
    /** 已认证但无权限访问资源 */
    FORBIDDEN(403, "无权限"),
    /** 资源不存在 */
    NOT_FOUND(404, "资源不存在"),
    /** 限流、滥用防护触发 */
    TOO_MANY_REQUESTS(429, "请求过于频繁"),
    /** 未捕获异常 */
    INTERNAL_ERROR(500, "服务器内部错误"),

    /** 按 ID 查用户不存在（内部或管理场景；对外登录请用 AUTH_LOGIN_FAILED） */
    USER_NOT_FOUND(1001, "用户不存在"),
    USERNAME_EXISTS(1002, "用户名已存在"),
    PHONE_EXISTS(1003, "手机号已注册"),
    EMAIL_EXISTS(1004, "邮箱已注册"),
    PASSWORD_ERROR(1005, "密码错误"),
    USER_DISABLED(1006, "用户已被禁用"),
    /** 登录接口统一错误，防止撞库枚举用户名 */
    AUTH_LOGIN_FAILED(1007, "用户名或密码错误"),

    TOKEN_INVALID(2001, "Token无效"),
    TOKEN_EXPIRED(2002, "Token已过期"),
    /** 登出后 access 进入黑名单 */
    TOKEN_BLACKLISTED(2003, "Token已被注销");

    /** 返回给前端的数字码 */
    private final int code;
    /** 默认中文提示 */
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}