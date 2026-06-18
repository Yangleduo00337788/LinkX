package com.linkx.server.module.chat.ws;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.linkx.server.entity.SysFriend;
import com.linkx.server.mapper.SysFriendMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatPresenceService {

    private final ChatSocketSessionRegistry sessionRegistry;
    private final ChatEventPushService pushService;
    private final SysFriendMapper friendMapper;

    public void onConnected(Long userId, WebSocketSession session) {
        boolean firstConnection = sessionRegistry.register(userId, session);
        pushService.sendToUser(
                userId,
                ChatEventType.CONNECTED,
                new ChatConnectedPayload(userId, LocalDateTime.now())
        );
        if (firstConnection) {
            broadcastPresence(userId, true);
        }
    }

    public void onDisconnected(Long userId, WebSocketSession session) {
        boolean lastConnection = sessionRegistry.unregister(userId, session);
        if (lastConnection) {
            broadcastPresence(userId, false);
        }
    }

    public boolean isOnline(Long userId) {
        return sessionRegistry.isOnline(userId);
    }

    private void broadcastPresence(Long userId, boolean online) {
        Set<Long> watcherUserIds = findWatcherUserIds(userId);
        if (watcherUserIds.isEmpty()) {
            return;
        }
        pushService.sendToUsers(
                watcherUserIds,
                ChatEventType.ONLINE_STATUS,
                new ChatOnlineStatusPayload(userId, online)
        );
    }

    private Set<Long> findWatcherUserIds(Long targetUserId) {
        LambdaQueryWrapper<SysFriend> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysFriend::getFriendId, targetUserId);
        return friendMapper.selectList(wrapper).stream()
                .map(SysFriend::getUserId)
                .filter(userId -> !userId.equals(targetUserId))
                .collect(Collectors.toSet());
    }
}
