package com.linkx.server.module.compliance.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ReportListItemDTO {
    private Long id;
    private Long reporterUserId;
    private String reporterUsername;
    private String targetType;
    private String targetId;
    private String reasonCategory;
    private String reasonDetail;
    private Integer status;
    private String resolution;
    private String resolutionNote;
    private Long handlerAdminId;
    private LocalDateTime handledTime;
    private LocalDateTime createTime;
}