package com.linkx.server.module.admin.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminDashboardReleaseSnapshotDTO {
    private String platform;
    private String version;
    private Integer published;
}