package com.linkx.server.common;

import lombok.Data;

/**
 * 统一 HTTP API 响应包装体。
 * <p>
 * 前端约定：{@code code == 200} 表示成功，其余为业务或 HTTP 语义错误码（见 {@link ErrorCode}）。
 * </p>
 *
 * @param <T> 业务数据类型；无数据时可为 {@code null}
 */
@Data
public class Result<T> {

    /** 业务状态码，200 为成功 */
    private int code;

    /** 提示文案，成功时为 "success" 或自定义 */
    private String message;

    /** 载荷；列表、对象、空均可 */
    private T data;

    /**
     * 成功响应并携带数据。
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        return result;
    }

    /**
     * 成功响应且无 data（如 void 操作）。
     */
    public static <T> Result<T> success() {
        return success(null);
    }

    /**
     * 失败响应，自定义码与文案。
     */
    public static <T> Result<T> error(int code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    /**
     * 失败响应，使用枚举中的码与默认文案。
     */
    public static <T> Result<T> error(ErrorCode errorCode) {
        return error(errorCode.getCode(), errorCode.getMessage());
    }
}