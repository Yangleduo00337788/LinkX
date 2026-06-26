package com.linkx.server.module.admin.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminLoginLogListItemDTO {
    private Long id;
    private Long userId;
    private String username;
    private String loginType;
    private String loginIp;
    private String loginLocation;
    private String userAgent;
    private Integer loginStatus;
    private String failReason;
    private LocalDateTime createTime;
}