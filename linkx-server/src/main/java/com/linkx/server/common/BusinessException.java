package com.linkx.server.common;  // 行注：声明当前文件所在包 com.linkx.server.common

import lombok.Getter;  // 行注：引入 Getter 类型

/**
 * 可预期的业务失败异常。
 * <p>
 * 由 {@link GlobalExceptionHandler} 捕获后转为 {@link Result} 返回客户端，
 * 不会记为 500 系统错误（除非未处理）。
 * </p>
 */
@Getter  // 行注：应用 @Getter 注解
// 行注：定义 BusinessException 类
public class BusinessException extends RuntimeException {

    /** 对应的错误码枚举 */
    private final ErrorCode errorCode;  // 行注：注入错误验证码依赖

    /**
     * 使用枚举默认文案作为异常消息。
     *
     * @param errorCode 业务错误码，不可为 null
     */
    // 行注：定义Business异常方法
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());  // 行注：调用获取消息
        this.errorCode = errorCode;  // 行注：初始化错误验证码
    }  // 行注：结束当前代码块

    /**
     * 使用自定义文案覆盖枚举默认 message（错误码仍取自枚举）。
     *
     * @param errorCode 业务错误码
     * @param message   返回给客户端的提示文案
     */
    // 行注：定义Business异常方法
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);  // 行注：调用super
        this.errorCode = errorCode;  // 行注：初始化错误验证码
    }  // 行注：结束当前代码块
}  // 行注：结束当前代码块
