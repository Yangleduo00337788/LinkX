package com.linkx.server.module.admin.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminBlacklistListItemDTO {
    private Long id;
    private Long userId;
    private Long blacklistUserId;
    private LocalDateTime createTime;
}