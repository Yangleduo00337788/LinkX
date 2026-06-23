package com.linkx.server.config;

import com.linkx.server.module.chat.ws.ChatHandshakeInterceptor;
import com.linkx.server.module.chat.ws.ChatWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * 注册聊天 WebSocket 端点 {@code /ws/chat}。
 * <p>
 * 握手阶段由 {@link ChatHandshakeInterceptor} 校验短期 ticket；允许的 Origin 与 HTTP CORS 一致。
 * </p>
 */
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final ChatWebSocketHandler chatWebSocketHandler;
    private final ChatHandshakeInterceptor chatHandshakeInterceptor;
    private final LinkxSecurityProperties linkxSecurityProperties;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatWebSocketHandler, "/ws/chat")
                .addInterceptors(chatHandshakeInterceptor)
                .setAllowedOriginPatterns(linkxSecurityProperties.getAllowedOriginPatterns().toArray(String[]::new));
    }
}