package com.linkx.server.module.admin.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminMessageListItemDTO {
    private Long id;
    private Long sessionId;
    private Long fromUserId;
    private Long toUserId;
    private Integer msgType;
    private String contentPreview;
    private LocalDateTime createTime;
}