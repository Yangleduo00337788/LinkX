package com.linkx.server.common;  // 行注：声明当前文件所在包 com.linkx.server.common

import lombok.extern.slf4j.Slf4j;  // 行注：引入 Slf4j 类型
import org.springframework.http.HttpStatus;  // 行注：引入 HttpStatus 类型
import org.springframework.security.authentication.BadCredentialsException;  // 行注：引入 BadCredentialsException 类型
import org.springframework.security.core.userdetails.UsernameNotFoundException;  // 行注：引入 UsernameNotFoundException 类型
import org.springframework.validation.FieldError;  // 行注：引入 FieldError 类型
import org.springframework.web.bind.MethodArgumentNotValidException;  // 行注：引入 MethodArgumentNotValidException 类型
import org.springframework.web.bind.MissingServletRequestParameterException;  // 行注：引入 MissingServletRequestParameterException 类型
import org.springframework.web.bind.annotation.ExceptionHandler;  // 行注：引入 ExceptionHandler 类型
import org.springframework.web.bind.annotation.ResponseStatus;  // 行注：引入 ResponseStatus 类型
import org.springframework.web.bind.annotation.RestControllerAdvice;  // 行注：引入 RestControllerAdvice 类型
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;  // 行注：引入 MethodArgumentTypeMismatchException 类型

import java.util.stream.Collectors;  // 行注：引入 Collectors 类型

/**
 * 全局 REST 异常处理：将各类异常统一封装为 {@link Result} JSON。
 * <p>
 * 业务异常返回 200 HTTP 状态 + 业务 code；部分参数错误返回 HTTP 400。
 * </p>
 */
@Slf4j  // 行注：应用 @Slf4j 注解
@RestControllerAdvice  // 行注：应用 @RestControllerAdvice 注解
// 行注：定义 GlobalExceptionHandler 类
public class GlobalExceptionHandler {

    /** 业务层主动抛出的 {@link BusinessException} */
    @ExceptionHandler(BusinessException.class)  // 行注：应用 @ExceptionHandler 注解
    // 行注：定义handleBusiness异常方法
    public Result<?> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());  // 行注：调用警告日志
        return Result.error(e.getErrorCode().getCode(), e.getMessage());  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /** Spring Security 用户不存在（部分旧路径可能触发） */
    @ExceptionHandler(UsernameNotFoundException.class)  // 行注：应用 @ExceptionHandler 注解
    // 行注：定义handleUsernameNotFound异常方法
    public Result<?> handleUsernameNotFoundException(UsernameNotFoundException e) {
        return Result.error(ErrorCode.USER_NOT_FOUND);  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /** Spring Security 密码不匹配 */
    @ExceptionHandler(BadCredentialsException.class)  // 行注：应用 @ExceptionHandler 注解
    // 行注：定义handleBadCredentials异常方法
    public Result<?> handleBadCredentialsException(BadCredentialsException e) {
        return Result.error(ErrorCode.PASSWORD_ERROR);  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /** {@code @Valid} 请求体验证失败 */
    @ExceptionHandler(MethodArgumentNotValidException.class)  // 行注：应用 @ExceptionHandler 注解
    @ResponseStatus(HttpStatus.BAD_REQUEST)  // 行注：应用 @ResponseStatus 注解
    // 行注：定义handleValidation异常方法
    public Result<?> handleValidationException(MethodArgumentNotValidException e) {
        // 行注：调用获取Binding结果
        String message = e.getBindingResult().getFieldErrors().stream()
                // 行注：继续调用映射
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));  // 行注：继续调用收集
        return Result.error(ErrorCode.BAD_REQUEST.getCode(), message);  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /** 缺少必填 Query/Form 参数 */
    @ExceptionHandler(MissingServletRequestParameterException.class)  // 行注：应用 @ExceptionHandler 注解
    @ResponseStatus(HttpStatus.BAD_REQUEST)  // 行注：应用 @ResponseStatus 注解
    // 行注：定义handleMissing参数方法
    public Result<?> handleMissingParam(MissingServletRequestParameterException e) {
        return Result.error(ErrorCode.BAD_REQUEST.getCode(), "缺少参数: " + e.getParameterName());  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /** 路径变量或参数类型无法转换（如非数字的 userId） */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)  // 行注：应用 @ExceptionHandler 注解
    @ResponseStatus(HttpStatus.BAD_REQUEST)  // 行注：应用 @ResponseStatus 注解
    // 行注：定义handle类型Mismatch方法
    public Result<?> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return Result.error(ErrorCode.BAD_REQUEST.getCode(), "参数类型错误: " + e.getName());  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /** 手动解析数字失败等场景 */
    @ExceptionHandler(NumberFormatException.class)  // 行注：应用 @ExceptionHandler 注解
    @ResponseStatus(HttpStatus.BAD_REQUEST)  // 行注：应用 @ResponseStatus 注解
    // 行注：定义handleNumber格式化异常方法
    public Result<?> handleNumberFormatException(NumberFormatException e) {
        return Result.error(ErrorCode.BAD_REQUEST.getCode(), "数字格式错误");  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /** 业务代码中主动抛出的非法参数（文案原样返回客户端） */
    @ExceptionHandler(IllegalArgumentException.class)  // 行注：应用 @ExceptionHandler 注解
    // 行注：定义handleIllegalArgument方法
    public Result<?> handleIllegalArgument(IllegalArgumentException e) {
        return Result.error(ErrorCode.BAD_REQUEST.getCode(), e.getMessage());  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /** 兜底：记录堆栈，对外隐藏细节 */
    @ExceptionHandler(Exception.class)  // 行注：应用 @ExceptionHandler 注解
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)  // 行注：应用 @ResponseStatus 注解
    // 行注：定义handle异常方法
    public Result<?> handleException(Exception e) {
        log.error("系统异常: {}", e.getMessage(), e);  // 行注：调用错误
        return Result.error(ErrorCode.INTERNAL_ERROR);  // 行注：返回处理结果
    }  // 行注：结束当前代码块
}  // 行注：结束当前代码块
