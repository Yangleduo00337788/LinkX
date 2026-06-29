package com.linkx.server.module.notification.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linkx.server.entity.SysUser;
import com.linkx.server.entity.SysUserNotification;
import com.linkx.server.mapper.SysUserMapper;
import com.linkx.server.mapper.SysUserNotificationMapper;
import com.linkx.server.module.chat.ws.ChatEventPushService;
import com.linkx.server.module.chat.ws.ChatEventType;
import com.linkx.server.module.notification.service.UserNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserNotificationServiceImpl implements UserNotificationService {

    private final SysUserNotificationMapper notificationMapper;
    private final SysUserMapper userMapper;
    private final ChatEventPushService chatEventPushService;

    @Override
    public void notifyUser(Long userId, String title, String content, String bizType, String bizId) {
        insertNotification(userId, title, content, bizType, bizId);
        pushUnreadHint(userId);
    }

    private void pushUnreadHint(Long userId) {
        if (userId == null) {
            return;
        }
        long unread = countUnread(userId);
        Map<String, Object> payload = new HashMap<>();
        payload.put("unreadCount", unread);
        chatEventPushService.sendToUser(userId, ChatEventType.NOTIFICATION, payload);
    }

    @Override
    public int broadcastToAllActiveUsers(String title, String content) {
        LambdaQueryWrapper<SysUser> w = new LambdaQueryWrapper<>();
        w.eq(SysUser::getStatus, 1).select(SysUser::getId);
        var users = userMapper.selectList(w);
        int count = 0;
        for (SysUser u : users) {
            if (u.getId() == null) {
                continue;
            }
            insertNotification(u.getId(), title, content, "SYSTEM_BROADCAST", null);
            count++;
        }
        for (SysUser u : users) {
            if (u.getId() != null) {
                pushUnreadHint(u.getId());
            }
        }
        return count;
    }

    private void insertNotification(Long userId, String title, String content, String bizType, String bizId) {
        SysUserNotification n = new SysUserNotification();
        n.setUserId(userId);
        n.setTitle(title);
        n.setContent(content);
        n.setBizType(bizType);
        n.setBizId(bizId);
        n.setReadFlag(0);
        notificationMapper.insert(n);
    }

    @Override
    public Page<SysUserNotification> pageForUser(Long userId, int page, int size) {
        LambdaQueryWrapper<SysUserNotification> w = new LambdaQueryWrapper<>();
        w.eq(SysUserNotification::getUserId, userId).orderByDesc(SysUserNotification::getCreateTime);
        return notificationMapper.selectPage(new Page<>(page, size), w);
    }

    @Override
    public void markRead(Long userId, Long notificationId) {
        SysUserNotification n = notificationMapper.selectById(notificationId);
        if (n == null || !userId.equals(n.getUserId())) {
            return;
        }
        n.setReadFlag(1);
        notificationMapper.updateById(n);
    }

    @Override
    public long countUnread(Long userId) {
        LambdaQueryWrapper<SysUserNotification> w = new LambdaQueryWrapper<>();
        w.eq(SysUserNotification::getUserId, userId).eq(SysUserNotification::getReadFlag, 0);
        return notificationMapper.selectCount(w);
    }

    @Override
    public int notifyUsers(List<Long> userIds, String title, String content) {
        if (userIds == null || userIds.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (Long uid : userIds) {
            if (uid == null) {
                continue;
            }
            insertNotification(uid, title, content, "SYSTEM_DIRECT", null);
            pushUnreadHint(uid);
            count++;
        }
        return count;
    }
}