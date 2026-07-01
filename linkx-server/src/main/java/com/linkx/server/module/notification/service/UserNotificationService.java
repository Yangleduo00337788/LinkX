package com.linkx.server.module.notification.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linkx.server.entity.SysUserNotification;

public interface UserNotificationService {

    void notifyUser(Long userId, String title, String content, String bizType, String bizId);

    /** 向所有未禁用用户写入同一条系统通知（桌面/Web 侧栏「通知」可见）。 */
    int broadcastToAllActiveUsers(String title, String content);

    Page<SysUserNotification> pageForUser(Long userId, int page, int size);

    void markRead(Long userId, Long notificationId);

    /** 将当前用户全部未读通知标为已读 */
    int markAllRead(Long userId);

    long countUnread(Long userId);

    /** 向指定用户 ID 列表发送同一条通知 */
    int notifyUsers(java.util.List<Long> userIds, String title, String content);
}