package com.linkx.server.module.admin.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminReleaseListItemDTO {
    private Long id;
    private String platform;
    private String version;
    private String downloadUrl;
    private String releaseNotes;
    private Integer forceUpdate;
    private Integer published;
    private LocalDateTime createTime;
}