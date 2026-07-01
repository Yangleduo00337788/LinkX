package com.linkx.server.module.notification.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linkx.server.common.Result;
import com.linkx.server.entity.SysUserNotification;
import com.linkx.server.module.notification.dto.NotificationUnreadDTO;
import com.linkx.server.module.notification.service.UserNotificationService;
import com.linkx.server.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class UserNotificationController {

    private final UserNotificationService notificationService;

    @GetMapping("/unread-count")
    public Result<NotificationUnreadDTO> unreadCount(@AuthenticationPrincipal UserDetails userDetails) {
        long n = notificationService.countUnread(SecurityUtils.resolveUserId(userDetails));
        return Result.success(new NotificationUnreadDTO(n));
    }

    @GetMapping
    public Result<Page<SysUserNotification>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        return Result.success(notificationService.pageForUser(SecurityUtils.resolveUserId(userDetails), page, size));
    }

    @PutMapping("/{id}/read")
    public Result<Void> markRead(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        notificationService.markRead(SecurityUtils.resolveUserId(userDetails), id);
        return Result.success();
    }

    @PutMapping("/read-all")
    public Result<Integer> markAllRead(@AuthenticationPrincipal UserDetails userDetails) {
        int n = notificationService.markAllRead(SecurityUtils.resolveUserId(userDetails));
        return Result.success(n);
    }
}