package com.linkx.server.module.admin.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminDashboardAlertDTO {
    /** INFO / WARN / ERROR */
    private String level;
    private String code;
    private String message;
}