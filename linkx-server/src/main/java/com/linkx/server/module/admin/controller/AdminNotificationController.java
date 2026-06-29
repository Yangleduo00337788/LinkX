package com.linkx.server.module.admin.controller;

import com.linkx.server.common.Result;
import com.linkx.server.module.admin.dto.AdminBroadcastNotificationRequest;
import com.linkx.server.module.admin.dto.AdminTargetNotificationRequest;
import com.linkx.server.module.notification.service.UserNotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/notifications")
@RequiredArgsConstructor
public class AdminNotificationController {

    private final UserNotificationService userNotificationService;

    /** 向所有启用中的用户写入系统通知，客户端侧栏「通知」可查看。 */
    @PostMapping("/broadcast")
    public Result<Map<String, Integer>> broadcast(@Valid @RequestBody AdminBroadcastNotificationRequest body) {
        int sent = userNotificationService.broadcastToAllActiveUsers(
                body.getTitle().trim(),
                body.getContent().trim());
        return Result.success(Map.of("sentCount", sent));
    }

    /** 向指定用户 ID 列表发送系统通知 */
    @PostMapping("/target")
    public Result<Map<String, Integer>> target(@Valid @RequestBody AdminTargetNotificationRequest body) {
        int sent = userNotificationService.notifyUsers(
                body.getUserIds(),
                body.getTitle().trim(),
                body.getContent().trim());
        return Result.success(Map.of("sentCount", sent));
    }
}