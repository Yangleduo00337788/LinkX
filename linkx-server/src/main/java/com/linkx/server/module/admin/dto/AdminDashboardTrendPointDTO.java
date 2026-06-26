package com.linkx.server.module.admin.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminDashboardTrendPointDTO {
    /** 日期 yyyy-MM-dd */
    private String date;
    private long newUsers;
    private long newMessages;
}