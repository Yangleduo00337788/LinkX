package com.linkx.server.common;  // 行注：声明当前文件所在包 com.linkx.server.common

import lombok.Getter;  // 行注：引入 Getter 类型

/**
 * 全局业务与 HTTP 语义错误码。
 * <p>
 * 4xx/5xx 段与 HTTP 习惯对齐；1xxx 用户域；2xxx 认证令牌域。
 * 登录失败对外统一使用 {@link #AUTH_LOGIN_FAILED}，避免区分「用户不存在」与「密码错误」。
 * </p>
 */
@Getter  // 行注：应用 @Getter 注解
// 行注：定义 ErrorCode 枚举
public enum ErrorCode {
    /** 成功（一般通过 Result.code=200 表示，枚举内保留便于对照） */
    // 行注：调用SUCCESS
    SUCCESS(200, "success"),
    /** 参数校验失败、非法业务参数 */
    // 行注：调用BAD请求
    BAD_REQUEST(400, "请求参数错误"),
    /** 未登录或令牌无效 */
    // 行注：调用UNAUTHORIZED
    UNAUTHORIZED(401, "未认证"),
    /** 已认证但无权限访问资源 */
    // 行注：调用FORBIDDEN
    FORBIDDEN(403, "无权限"),
    /** 资源不存在 */
    // 行注：调用NOTFOUND
    NOT_FOUND(404, "资源不存在"),
    /** 限流、滥用防护触发 */
    // 行注：调用TOOMANY请求
    TOO_MANY_REQUESTS(429, "请求过于频繁"),
    /** 未捕获异常 */
    // 行注：调用INTERNAL错误
    INTERNAL_ERROR(500, "服务器内部错误"),

    /** 按 ID 查用户不存在（内部或管理场景；对外登录请用 AUTH_LOGIN_FAILED） */
    // 行注：调用用户NOTFOUND
    USER_NOT_FOUND(1001, "用户不存在"),
    // 行注：调用USERNAME判断是否存在
    USERNAME_EXISTS(1002, "用户名已存在"),
    // 行注：调用PHONE判断是否存在
    PHONE_EXISTS(1003, "手机号已注册"),
    // 行注：调用EMAIL判断是否存在
    EMAIL_EXISTS(1004, "邮箱已注册"),
    // 行注：调用密码错误
    PASSWORD_ERROR(1005, "密码错误"),
    // 行注：调用用户DISABLED
    USER_DISABLED(1006, "用户已被禁用"),
    /** 登录接口统一错误，防止撞库枚举用户名 */
    // 行注：调用认证登录FAILED
    AUTH_LOGIN_FAILED(1007, "用户名或密码错误"),

    // 行注：调用令牌INVALID
    TOKEN_INVALID(2001, "Token无效"),
    // 行注：调用令牌EXPIRED
    TOKEN_EXPIRED(2002, "Token已过期"),
    /** 登出后 access 进入黑名单 */
    TOKEN_BLACKLISTED(2003, "Token已被注销");  // 行注：调用令牌BLACKLISTED

    /** 返回给前端的数字码 */
    private final int code;  // 行注：注入验证码依赖
    /** 默认中文提示 */
    private final String message;  // 行注：注入消息依赖

    /**
     * 枚举常量构造器。
     *
     * @param code    HTTP 或业务数字码
     * @param message 默认中文说明
     */
    // 行注：调用错误验证码
    ErrorCode(int code, String message) {
        this.code = code;  // 行注：初始化验证码
        this.message = message;  // 行注：初始化消息
    }  // 行注：结束当前代码块
}  // 行注：结束当前代码块
