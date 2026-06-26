package com.linkx.server.module.chat.ws;  // 行注：声明当前文件所在包 com.linkx.server.module.chat.ws

import org.springframework.stereotype.Component;  // 行注：引入 Component 类型
import org.springframework.web.socket.WebSocketSession;  // 行注：引入 WebSocketSession 类型

import java.io.IOException;  // 行注：引入 IOException 类型
import java.util.ArrayList;  // 行注：引入 ArrayList 类型
import java.util.List;  // 行注：引入 List 类型
import java.util.Map;  // 行注：引入 Map 类型
import java.util.Set;  // 行注：引入 Set 类型
import java.util.concurrent.ConcurrentHashMap;  // 行注：引入 ConcurrentHashMap 类型

/**
 * 在线 WebSocket 会话注册表：同一用户可多端连接，用于按 userId 广播推送。
 */
@Component  // 行注：应用 @Component 注解
// 行注：定义 ChatSocketSessionRegistry 类
public class ChatSocketSessionRegistry {

    private final Map<Long, Set<WebSocketSession>> userSessions = new ConcurrentHashMap<>();  // 行注：注入用户会话列表依赖

    /**
     * 登记会话；返回 true 表示该用户从离线变为至少一条连接（首个端上线）。
     */
    // 行注：定义注册方法
    public boolean register(Long userId, WebSocketSession session) {
        // 同一用户可能在多端同时连接，因此 value 设计成 session 集合而不是单值。
        Set<WebSocketSession> sessions = userSessions.computeIfAbsent(userId, key -> ConcurrentHashMap.newKeySet());  // 行注：执行初始化操作
        sessions.add(session);  // 行注：调用添加
        // 返回“是否首个连接”，供在线状态服务决定是否要广播上线事件。
        return sessions.size() == 1;  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /** 移除会话；返回 true 表示该用户已无在线连接（完全下线） */
    // 行注：定义unregister方法
    public boolean unregister(Long userId, WebSocketSession session) {
        Set<WebSocketSession> sessions = userSessions.get(userId);  // 行注：初始化会话列表
        // 行注：判断是否满足当前条件
        if (sessions == null) {
            return false;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        sessions.remove(session);  // 行注：调用移除
        // 行注：判断是否满足当前条件
        if (sessions.isEmpty()) {
            // 最后一条连接断开时，把用户整个 key 清掉，避免注册表持续膨胀。
            userSessions.remove(userId);  // 行注：调用移除
            return true;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        return false;  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /**
     * 判断在线。
     *
     * @param userId 用户 ID
     * @return 是否满足条件
     */
    // 行注：定义是否在线方法
    public boolean isOnline(Long userId) {
        Set<WebSocketSession> sessions = userSessions.get(userId);  // 行注：初始化会话列表
        return sessions != null && !sessions.isEmpty();  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /** 当前至少有一条 WebSocket 连接的去重用户数（管理后台概览） */
    public int getOnlineUserCount() {
        return userSessions.size();
    }

    /**
     * 获取用户会话。
     *
     * @param userId 用户 ID
     * @return Web连接会话列表
     */
    // 行注：定义获取用户会话列表方法
    public List<WebSocketSession> getUserSessions(Long userId) {
        Set<WebSocketSession> sessions = userSessions.get(userId);  // 行注：初始化会话列表
        // 行注：判断是否满足当前条件
        if (sessions == null || sessions.isEmpty()) {
            return List.of();  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        // 返回副本而不是原集合，避免外部遍历时与并发注册/移除相互影响。
        return new ArrayList<>(sessions);  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /**
     * 处理closeQuietly。
     *
     * @param session 会话
     */
    // 行注：定义closeQuietly方法
    public void closeQuietly(WebSocketSession session) {
        // 行注：尝试执行可能失败的逻辑
        try {
            session.close();  // 行注：调用close
        // 行注：执行当前方法调用
        } catch (IOException ignored) {
            // 这里是清理路径，不需要把关闭异常继续向上冒泡。
        }  // 行注：结束当前代码块
    }  // 行注：结束当前代码块
}  // 行注：结束当前代码块
