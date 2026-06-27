package com.linkx.server.module.session.dto;

import lombok.Data;

@Data
public class UpdateSessionPreferencesRequest {
    private String sessionRemark;
    private Boolean pinned;
    private Boolean notificationMuted;
}