package com.linkx.server.module.notification.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linkx.server.entity.SysUserNotification;

public interface UserNotificationService {

    void notifyUser(Long userId, String title, String content, String bizType, String bizId);

    /** 向多个用户写入同一条通知，返回成功写入人数。 */
    int notifyUsers(java.util.Collection<Long> userIds, String title, String content, String bizType, String bizId);

    /** 向所有未禁用用户写入同一条系统通知（桌面/Web 侧栏「通知」可见）。 */
    int broadcastToAllActiveUsers(String title, String content);

    Page<SysUserNotification> pageForUser(Long userId, int page, int size);

    long countUnread(Long userId);

    void markRead(Long userId, Long notificationId);
}