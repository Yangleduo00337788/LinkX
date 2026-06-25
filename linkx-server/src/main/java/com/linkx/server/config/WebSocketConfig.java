package com.linkx.server.config;  // 行注：声明当前文件所在包 com.linkx.server.config

import com.linkx.server.module.chat.ws.ChatHandshakeInterceptor;  // 行注：引入 ChatHandshakeInterceptor 类型
import com.linkx.server.module.chat.ws.ChatWebSocketHandler;  // 行注：引入 ChatWebSocketHandler 类型
import lombok.RequiredArgsConstructor;  // 行注：引入 RequiredArgsConstructor 类型
import org.springframework.context.annotation.Configuration;  // 行注：引入 Configuration 类型
import org.springframework.web.socket.config.annotation.EnableWebSocket;  // 行注：引入 EnableWebSocket 类型
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;  // 行注：引入 WebSocketConfigurer 类型
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;  // 行注：引入 WebSocketHandlerRegistry 类型

/**
 * 注册聊天 WebSocket 端点 {@code /ws/chat}。
 * <p>
 * 握手阶段由 {@link ChatHandshakeInterceptor} 校验短期 ticket；允许的 Origin 与 HTTP CORS 一致。
 * </p>
 */
@Configuration  // 行注：应用 @Configuration 注解
@EnableWebSocket  // 行注：应用 @EnableWebSocket 注解
@RequiredArgsConstructor  // 行注：应用 @RequiredArgsConstructor 注解
// 行注：定义 WebSocketConfig 类
public class WebSocketConfig implements WebSocketConfigurer {

    private final ChatWebSocketHandler chatWebSocketHandler;  // 行注：注入聊天Web连接处理器依赖
    private final ChatHandshakeInterceptor chatHandshakeInterceptor;  // 行注：注入聊天Handshake拦截器依赖
    private final LinkxSecurityProperties linkxSecurityProperties;  // 行注：注入LinkX 安全属性依赖

    /**
     * 注册 WebSocket 处理器与握手拦截器。
     *
     * @param registry WebSocket 处理器注册器
     */
    @Override  // 行注：应用 @Override 注解
    // 行注：定义注册Web连接Handlers方法
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 行注：调用添加处理器
        registry.addHandler(chatWebSocketHandler, "/ws/chat")
                // 行注：继续调用添加Interceptors
                .addInterceptors(chatHandshakeInterceptor)
                .setAllowedOriginPatterns(linkxSecurityProperties.getAllowedOriginPatterns().toArray(String[]::new));  // 行注：继续调用设置允许的来源模式
    }  // 行注：结束当前代码块
}  // 行注：结束当前代码块
