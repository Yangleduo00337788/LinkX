package com.linkx.server.module.admin.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminAuthResponse {
    private String accessToken;
    private Long adminId;
    private String username;
    private String displayName;
    private String role;
}