package com.linkx.server.module.admin.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminDashboardMessageTypeSliceDTO {
    private int msgType;
    private String label;
    private long count;
}