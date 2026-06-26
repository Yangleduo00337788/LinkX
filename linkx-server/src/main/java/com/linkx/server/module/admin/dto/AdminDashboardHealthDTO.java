package com.linkx.server.module.admin.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminDashboardHealthDTO {
    private String mysql;
    private String redis;
    private String minio;
}