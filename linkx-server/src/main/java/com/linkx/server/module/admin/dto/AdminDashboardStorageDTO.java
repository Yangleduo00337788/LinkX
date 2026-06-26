package com.linkx.server.module.admin.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminDashboardStorageDTO {
    private long fileCount;
    private long totalBytes;
}