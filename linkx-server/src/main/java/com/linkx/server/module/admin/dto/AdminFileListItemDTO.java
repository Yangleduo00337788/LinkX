package com.linkx.server.module.admin.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminFileListItemDTO {
    private Long id;
    private Long userId;
    private String originalName;
    private Long fileSize;
    private String fileType;
    private LocalDateTime createTime;
}