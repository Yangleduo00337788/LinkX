package com.linkx.server.module.chat.ws;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 在线 WebSocket 会话注册表：同一用户可多端连接，用于按 userId 广播推送。
 */
@Component
public class ChatSocketSessionRegistry {

    private final Map<Long, Set<WebSocketSession>> userSessions = new ConcurrentHashMap<>();

    public boolean register(Long userId, WebSocketSession session) {
        Set<WebSocketSession> sessions = userSessions.computeIfAbsent(userId, key -> ConcurrentHashMap.newKeySet());
        sessions.add(session);
        return sessions.size() == 1;
    }

    public boolean unregister(Long userId, WebSocketSession session) {
        Set<WebSocketSession> sessions = userSessions.get(userId);
        if (sessions == null) {
            return false;
        }
        sessions.remove(session);
        if (sessions.isEmpty()) {
            userSessions.remove(userId);
            return true;
        }
        return false;
    }

    public boolean isOnline(Long userId) {
        Set<WebSocketSession> sessions = userSessions.get(userId);
        return sessions != null && !sessions.isEmpty();
    }

    public List<WebSocketSession> getUserSessions(Long userId) {
        Set<WebSocketSession> sessions = userSessions.get(userId);
        if (sessions == null || sessions.isEmpty()) {
            return List.of();
        }
        return new ArrayList<>(sessions);
    }

    public void closeQuietly(WebSocketSession session) {
        try {
            session.close();
        } catch (IOException ignored) {
        }
    }
}
