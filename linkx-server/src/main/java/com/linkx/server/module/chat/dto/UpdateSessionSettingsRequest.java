package com.linkx.server.module.chat.dto;

import lombok.Data;

@Data
public class UpdateSessionSettingsRequest {
    private Long targetId;
    private Integer sessionType;
    private Boolean pinned;
    private Boolean notificationMuted;
    private String sessionRemark;
}