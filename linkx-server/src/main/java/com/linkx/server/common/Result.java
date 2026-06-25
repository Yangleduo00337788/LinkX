package com.linkx.server.common;  // 行注：声明当前文件所在包 com.linkx.server.common

import lombok.Data;  // 行注：引入 Data 类型

/**
 * 统一 HTTP API 响应包装体。
 * <p>
 * 前端约定：{@code code == 200} 表示成功，其余为业务或 HTTP 语义错误码（见 {@link ErrorCode}）。
 * </p>
 *
 * @param <T> 业务数据类型；无数据时可为 {@code null}
 */
@Data  // 行注：应用 @Data 注解
// 行注：定义 Result 类
public class Result<T> {

    /** 业务状态码，200 为成功 */
    private int code;  // 行注：声明验证码字段

    /** 提示文案，成功时为 "success" 或自定义 */
    private String message;  // 行注：声明消息字段

    /** 载荷；列表、对象、空均可 */
    private T data;  // 行注：声明data字段

    /**
     * 成功响应并携带数据。
     *
     * @param data 业务载荷，可为 null
     * @param <T>  载荷类型
     * @return code=200、message=success 的包装体
     */
    // 行注：定义success方法
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();  // 行注：初始化结果
        result.setCode(200);  // 行注：调用设置验证码
        result.setMessage("success");  // 行注：调用设置消息
        result.setData(data);  // 行注：调用设置Data
        return result;  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /**
     * 成功响应且无 data（如 void 操作）。
     *
     * @param <T> 占位泛型
     * @return code=200、data=null
     */
    // 行注：定义success方法
    public static <T> Result<T> success() {
        return success(null);  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /**
     * 失败响应，自定义码与文案。
     *
     * @param code    业务错误码
     * @param message 提示文案
     * @param <T>     占位泛型（失败时通常无 data）
     */
    // 行注：定义错误方法
    public static <T> Result<T> error(int code, String message) {
        Result<T> result = new Result<>();  // 行注：初始化结果
        result.setCode(code);  // 行注：调用设置验证码
        result.setMessage(message);  // 行注：调用设置消息
        return result;  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /**
     * 失败响应，使用枚举中的码与默认文案。
     *
     * @param errorCode 预定义错误码
     * @param <T>       占位泛型
     */
    // 行注：定义错误方法
    public static <T> Result<T> error(ErrorCode errorCode) {
        return error(errorCode.getCode(), errorCode.getMessage());  // 行注：返回处理结果
    }  // 行注：结束当前代码块
}  // 行注：结束当前代码块
