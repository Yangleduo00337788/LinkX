package com.linkx.server.module.compliance.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MyReportListItemDTO {
    private Long id;
    private String targetType;
    private String targetId;
    private String reasonCategory;
    private Integer status;
    private String resolutionNote;
    private LocalDateTime handledTime;
    private LocalDateTime createTime;
}