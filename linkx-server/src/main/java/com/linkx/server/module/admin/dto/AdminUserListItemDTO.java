package com.linkx.server.module.admin.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminUserListItemDTO {
    private Long id;
    private String username;
    private String nickname;
    private Integer status;
    private LocalDateTime createTime;
}