package com.linkx.server.module.notification.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linkx.server.entity.SysUserNotification;
import com.linkx.server.mapper.SysUserNotificationMapper;
import com.linkx.server.module.notification.service.UserNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserNotificationServiceImpl implements UserNotificationService {

    private final SysUserNotificationMapper notificationMapper;

    @Override
    public void notifyUser(Long userId, String title, String content, String bizType, String bizId) {
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
}