package com.linkx.server.module.admin.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminAuditLogListItemDTO {
    private Long id;
    private String adminUsername;
    private String action;
    private String targetType;
    private String targetId;
    private String detail;
    private String clientIp;
    private LocalDateTime createTime;
}