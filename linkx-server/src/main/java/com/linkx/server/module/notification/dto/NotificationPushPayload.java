package com.linkx.server.module.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** WebSocket NOTIFICATION 事件 payload（客户端刷新未读角标）。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPushPayload {
    private Long notificationId;
    private String title;
    private String bizType;
}