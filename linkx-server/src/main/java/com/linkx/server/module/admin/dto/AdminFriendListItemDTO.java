package com.linkx.server.module.admin.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminFriendListItemDTO {
    private Long id;
    private Long userId;
    private Long friendId;
    private String friendUsername;
    private String friendNickname;
    private String remark;
    private LocalDateTime createTime;
}