package com.linkx.server.module.chat.ws;  // 行注：声明当前文件所在包 com.linkx.server.module.chat.ws

import com.fasterxml.jackson.core.JsonProcessingException;  // 行注：引入 JsonProcessingException 类型
import com.fasterxml.jackson.databind.ObjectMapper;  // 行注：引入 ObjectMapper 类型
import lombok.RequiredArgsConstructor;  // 行注：引入 RequiredArgsConstructor 类型
import lombok.extern.slf4j.Slf4j;  // 行注：引入 Slf4j 类型
import org.springframework.stereotype.Service;  // 行注：引入 Service 类型
import org.springframework.web.socket.TextMessage;  // 行注：引入 TextMessage 类型
import org.springframework.web.socket.WebSocketSession;  // 行注：引入 WebSocketSession 类型

import java.io.IOException;  // 行注：引入 IOException 类型
import java.util.Collection;  // 行注：引入 Collection 类型

/**
 * 将 {@link ChatRealtimeEvent} 序列化为 JSON 文本帧，推送给指定用户或用户集合的所有在线连接。
 */
@Slf4j  // 行注：应用 @Slf4j 注解
@Service  // 行注：应用 @Service 注解
@RequiredArgsConstructor  // 行注：应用 @RequiredArgsConstructor 注解
// 行注：定义 ChatEventPushService 类
public class ChatEventPushService {

    private final ObjectMapper objectMapper;  // 行注：注入objectMapper依赖
    private final ChatSocketSessionRegistry sessionRegistry;  // 行注：注入会话注册表依赖

    /** 向某用户所有在线 WebSocket 连接推送同一事件 */
    // 行注：定义发送转为用户方法
    public void sendToUser(Long userId, String type, Object data) {
        // 先序列化一次，再复用到同一用户的多端连接，避免重复 JSON 编码。
        String payload = serialize(type, data);  // 行注：初始化载荷
        // 行注：判断是否满足当前条件
        if (payload == null) {
            return;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        // 行注：遍历当前集合或范围
        for (WebSocketSession session : sessionRegistry.getUserSessions(userId)) {
            sendMessage(session, payload);  // 行注：调用发送消息
        }  // 行注：结束当前代码块
    }  // 行注：结束当前代码块

    /** 批量用户推送（如群消息、群撤回） */
    // 行注：定义发送转为Users方法
    public void sendToUsers(Collection<Long> userIds, String type, Object data) {
        // 行注：判断是否满足当前条件
        if (userIds == null || userIds.isEmpty()) {
            return;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        // 批量推送同样只做一次序列化，降低群广播时的 CPU 消耗。
        String payload = serialize(type, data);  // 行注：初始化载荷
        // 行注：判断是否满足当前条件
        if (payload == null) {
            return;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        // 行注：遍历当前集合或范围
        for (Long userId : userIds) {
            // 行注：遍历当前集合或范围
            for (WebSocketSession session : sessionRegistry.getUserSessions(userId)) {
                sendMessage(session, payload);  // 行注：调用发送消息
            }  // 行注：结束当前代码块
        }  // 行注：结束当前代码块
    }  // 行注：结束当前代码块

    // 行注：定义serialize方法
    private String serialize(String type, Object data) {
        // 行注：尝试执行可能失败的逻辑
        try {
            return objectMapper.writeValueAsString(new ChatRealtimeEvent(type, data));  // 行注：返回处理结果
        // 行注：执行当前方法调用
        } catch (JsonProcessingException e) {
            log.error("Serialize realtime event failed, type={}", type, e);  // 行注：执行初始化操作
            return null;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
    }  // 行注：结束当前代码块

    // 行注：定义发送消息方法
    private void sendMessage(WebSocketSession session, String payload) {
        // 行注：判断是否满足当前条件
        if (session == null || !session.isOpen()) {
            return;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        // 行注：调用synchronized
        synchronized (session) {
            // 行注：尝试执行可能失败的逻辑
            try {
                session.sendMessage(new TextMessage(payload));  // 行注：调用发送消息
            // 行注：执行当前方法调用
            } catch (IOException e) {
                log.warn("Send websocket message failed, sessionId={}", session.getId(), e);  // 行注：执行初始化操作
                // 发送失败通常意味着连接已不可用，顺手从注册表移除，避免后续重复失败。
                sessionRegistry.closeQuietly(session);  // 行注：调用closeQuietly
            }  // 行注：结束当前代码块
        }  // 行注：结束当前代码块
    }  // 行注：结束当前代码块
}  // 行注：结束当前代码块
