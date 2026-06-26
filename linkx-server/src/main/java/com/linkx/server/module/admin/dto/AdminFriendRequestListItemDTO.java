package com.linkx.server.module.admin.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminFriendRequestListItemDTO {
    private Long id;
    private Long fromUserId;
    private String fromUsername;
    private Long toUserId;
    private String toUsername;
    private String message;
    /** 0待处理 1已同意 2已拒绝 */
    private Integer status;
    private LocalDateTime createTime;
}