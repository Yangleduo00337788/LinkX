package com.linkx.server.module.admin.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminDashboardGroupRankDTO {
    private Long groupId;
    private String groupName;
    private long memberCount;
    private long messageCount7d;
}