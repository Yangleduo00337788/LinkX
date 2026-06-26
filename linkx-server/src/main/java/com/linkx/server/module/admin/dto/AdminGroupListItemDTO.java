package com.linkx.server.module.admin.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminGroupListItemDTO {
    private Long id;
    private String groupName;
    private Long ownerId;
    private Integer maxMembers;
    private LocalDateTime createTime;
}