package com.linkx.server.module.chat.ws;  // 行注：声明当前文件所在包 com.linkx.server.module.chat.ws

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;  // 行注：引入 LambdaQueryWrapper 类型
import com.linkx.server.entity.SysFriend;  // 行注：引入 SysFriend 类型
import com.linkx.server.mapper.SysFriendMapper;  // 行注：引入 SysFriendMapper 类型
import lombok.RequiredArgsConstructor;  // 行注：引入 RequiredArgsConstructor 类型
import org.springframework.stereotype.Service;  // 行注：引入 Service 类型
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;  // 行注：引入 WebSocketSession 类型

import java.time.LocalDateTime;  // 行注：引入 LocalDateTime 类型
import java.util.Set;  // 行注：引入 Set 类型
import java.util.stream.Collectors;  // 行注：引入 Collectors 类型

/**
 * 好友在线状态：连接/断开时向好友广播 online/offline 事件。
 */
@Service  // 行注：应用 @Service 注解
@RequiredArgsConstructor  // 行注：应用 @RequiredArgsConstructor 注解
// 行注：定义 ChatPresenceService 类
public class ChatPresenceService {

    private final ChatSocketSessionRegistry sessionRegistry;  // 行注：注入会话注册表依赖
    private final ChatEventPushService pushService;  // 行注：注入推送服务依赖
    private final SysFriendMapper friendMapper;  // 行注：注入好友Mapper依赖

    /** 注册连接；若是该用户首个会话则向好友广播上线 */
    // 行注：定义on已连接方法
    public void onConnected(Long userId, WebSocketSession session) {
        // 同一用户可能多端同时在线，只有“首个连接”才需要广播上线状态。
        boolean firstConnection = sessionRegistry.register(userId, session);  // 行注：初始化firstConnection
        // 行注：补充当前表达式片段
        pushService.sendToUser(
                // 行注：补充当前表达式片段
                userId,
                // 行注：补充当前表达式片段
                ChatEventType.CONNECTED,
                // 行注：调用now
                new ChatConnectedPayload(userId, LocalDateTime.now())
        );  // 行注：结束当前参数配置
        // 行注：判断是否满足当前条件
        if (firstConnection) {
            broadcastPresence(userId, true);  // 行注：调用广播在线状态
        }  // 行注：结束当前代码块
    }  // 行注：结束当前代码块

    /** 移除连接；若是该用户最后一个会话则向好友广播下线 */
    // 行注：定义onDisconnected方法
    public void onDisconnected(Long userId, WebSocketSession session) {
        // 只有最后一个连接断开时才广播下线，避免多端场景中状态频繁抖动。
        boolean lastConnection = sessionRegistry.unregister(userId, session);  // 行注：初始化最后Connection
        // 行注：判断是否满足当前条件
        if (lastConnection) {
            broadcastPresence(userId, false);  // 行注：调用广播在线状态
        }  // 行注：结束当前代码块
    }  // 行注：结束当前代码块

    /** 是否存在至少一条已注册的 WebSocket 连接 */
    // 行注：定义是否在线方法
    public boolean isOnline(Long userId) {
        return sessionRegistry.isOnline(userId);  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /** 管理员踢人：通知客户端并关闭该用户全部 WebSocket */
    public void disconnectAllForUser(Long userId, String reason) {
        if (userId == null) {
            return;
        }
        pushService.sendToUser(userId, ChatEventType.FORCE_LOGOUT, new ChatForceLogoutPayload(reason));
        for (WebSocketSession session : sessionRegistry.getUserSessions(userId)) {
            try {
                session.close(CloseStatus.NORMAL.withReason(reason != null ? reason : "kicked"));
            } catch (Exception ignored) {
                sessionRegistry.closeQuietly(session);
            }
        }
        broadcastPresence(userId, false);
    }

    // 行注：定义广播在线状态方法
    private void broadcastPresence(Long userId, boolean online) {
        // 只通知真正“关注该用户在线状态”的好友集合，避免无意义全量广播。
        Set<Long> watcherUserIds = findWatcherUserIds(userId);  // 行注：初始化watcher用户ID列表
        // 行注：判断是否满足当前条件
        if (watcherUserIds.isEmpty()) {
            return;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        // 行注：补充当前表达式片段
        pushService.sendToUsers(
                // 行注：补充当前表达式片段
                watcherUserIds,
                // 行注：补充当前表达式片段
                ChatEventType.ONLINE_STATUS,
                // 行注：调用聊天在线状态载荷
                new ChatOnlineStatusPayload(userId, online)
        );  // 行注：结束当前参数配置
    }  // 行注：结束当前代码块

    // 行注：定义查找Watcher用户ID列表方法
    private Set<Long> findWatcherUserIds(Long targetUserId) {
        LambdaQueryWrapper<SysFriend> wrapper = new LambdaQueryWrapper<>();  // 行注：初始化条件封装器
        wrapper.eq(SysFriend::getFriendId, targetUserId);  // 行注：调用等值条件
        return friendMapper.selectList(wrapper).stream()  // 行注：返回处理结果
                // 行注：继续调用映射
                .map(SysFriend::getUserId)
                // 行注：继续调用过滤
                .filter(userId -> !userId.equals(targetUserId))
                .collect(Collectors.toSet());  // 行注：继续调用收集
    }  // 行注：结束当前代码块
}  // 行注：结束当前代码块
