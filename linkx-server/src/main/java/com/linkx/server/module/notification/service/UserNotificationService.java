package com.linkx.server.module.notification.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linkx.server.entity.SysUserNotification;

public interface UserNotificationService {

    void notifyUser(Long userId, String title, String content, String bizType, String bizId);

    Page<SysUserNotification> pageForUser(Long userId, int page, int size);

    void markRead(Long userId, Long notificationId);
}