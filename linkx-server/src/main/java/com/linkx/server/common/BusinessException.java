package com.linkx.server.common;

import lombok.Getter;

/**
 * 可预期的业务失败异常。
 * <p>
 * 由 {@link GlobalExceptionHandler} 捕获后转为 {@link Result} 返回客户端，
 * 不会记为 500 系统错误（除非未处理）。
 * </p>
 */
@Getter
public class BusinessException extends RuntimeException {

    /** 对应的错误码枚举 */
    private final ErrorCode errorCode;

    /**
     * 使用枚举默认文案作为异常消息。
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    /**
     * 使用自定义文案覆盖枚举默认 message（错误码仍取自枚举）。
     */
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}