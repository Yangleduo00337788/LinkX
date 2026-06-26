package com.linkx.server.module.admin.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminGroupRequestListItemDTO {
    private Long id;
    private Long groupId;
    private String groupName;
    private Long fromUserId;
    private Long toUserId;
    private Integer requestType;
    private String message;
    private Integer status;
    private LocalDateTime createTime;
}