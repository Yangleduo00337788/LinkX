package com.linkx.server.security;  // 行注：声明当前文件所在包 com.linkx.server.security

import com.fasterxml.jackson.databind.ObjectMapper;  // 行注：引入 ObjectMapper 类型
import com.linkx.server.common.ErrorCode;  // 行注：引入 ErrorCode 类型
import com.linkx.server.common.Result;  // 行注：引入 Result 类型
import jakarta.servlet.http.HttpServletRequest;  // 行注：引入 HttpServletRequest 类型
import jakarta.servlet.http.HttpServletResponse;  // 行注：引入 HttpServletResponse 类型
import lombok.RequiredArgsConstructor;  // 行注：引入 RequiredArgsConstructor 类型
import org.springframework.http.MediaType;  // 行注：引入 MediaType 类型
import org.springframework.security.core.AuthenticationException;  // 行注：引入 AuthenticationException 类型
import org.springframework.security.web.AuthenticationEntryPoint;  // 行注：引入 AuthenticationEntryPoint 类型
import org.springframework.stereotype.Component;  // 行注：引入 Component 类型

import java.io.IOException;  // 行注：引入 IOException 类型

/**
 * 未认证访问受保护资源时的入口：返回 HTTP 401 + JSON {@link Result}，而非重定向登录页。
 */
@Component  // 行注：应用 @Component 注解
@RequiredArgsConstructor  // 行注：应用 @RequiredArgsConstructor 注解
// 行注：定义 JwtAuthenticationEntryPoint 类
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;  // 行注：注入objectMapper依赖

    /**
     * Spring Security 回调：写入统一未认证 JSON 响应。
     */
    @Override  // 行注：应用 @Override 注解
    // 行注：定义commence方法
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {  // 行注：抛出异常并中断当前流程
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);  // 行注：调用设置状态
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);  // 行注：调用设置内容类型
        objectMapper.writeValue(response.getOutputStream(), Result.error(ErrorCode.UNAUTHORIZED));  // 行注：调用write值
    }  // 行注：结束当前代码块
}  // 行注：结束当前代码块
