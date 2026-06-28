package com.linkx.server.module.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminBroadcastNotificationRequest {
    @NotBlank
    private String title;
    @NotBlank
    private String content;
}