package com.linkx.server.module.chat.ws;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Collection;

/**
 * 将 {@link ChatRealtimeEvent} 序列化为 JSON 文本帧，推送给指定用户或用户集合的所有在线连接。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatEventPushService {

    private final ObjectMapper objectMapper;
    private final ChatSocketSessionRegistry sessionRegistry;

    public void sendToUser(Long userId, String type, Object data) {
        String payload = serialize(type, data);
        if (payload == null) {
            return;
        }
        for (WebSocketSession session : sessionRegistry.getUserSessions(userId)) {
            sendMessage(session, payload);
        }
    }

    public void sendToUsers(Collection<Long> userIds, String type, Object data) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        String payload = serialize(type, data);
        if (payload == null) {
            return;
        }
        for (Long userId : userIds) {
            for (WebSocketSession session : sessionRegistry.getUserSessions(userId)) {
                sendMessage(session, payload);
            }
        }
    }

    private String serialize(String type, Object data) {
        try {
            return objectMapper.writeValueAsString(new ChatRealtimeEvent(type, data));
        } catch (JsonProcessingException e) {
            log.error("Serialize realtime event failed, type={}", type, e);
            return null;
        }
    }

    private void sendMessage(WebSocketSession session, String payload) {
        if (session == null || !session.isOpen()) {
            return;
        }
        synchronized (session) {
            try {
                session.sendMessage(new TextMessage(payload));
            } catch (IOException e) {
                log.warn("Send websocket message failed, sessionId={}", session.getId(), e);
                sessionRegistry.closeQuietly(session);
            }
        }
    }
}
