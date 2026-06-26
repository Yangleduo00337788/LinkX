package com.linkx.server.module.admin.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AdminDashboardStatsDTO {
    private long userCount;
    private long groupCount;
    private long messageCount;
    private long fileCount;
    private long todayNewUsers;
    private long todayNewMessages;
    /** 今日去重登录用户数（登录日志） */
    private long todayLoginUserCount;
    /** 今日去重发消息用户数 */
    private long todayActiveMessageUserCount;
    /** 禁用账号数 */
    private long disabledUserCount;
    /** 黑名单记录数 */
    private long blacklistCount;
    /** 今日管理员强制下线次数 */
    private long todayKickCount;

    private long pendingFriendRequests;
    private long pendingGroupRequests;

    private int trendDays;
    private List<AdminDashboardTrendPointDTO> trend;

    private AdminDashboardCompareDTO compare;
    private List<AdminDashboardMessageTypeSliceDTO> messageTypeBreakdown;
    private List<AdminDashboardGroupRankDTO> topGroupsByMembers;
    private List<AdminDashboardGroupRankDTO> topGroupsByMessages7d;
    private List<AdminAuditLogListItemDTO> recentAuditLogs;
    private long onlineUserCount;
    private AdminDashboardHealthDTO health;
    private List<AdminDashboardAlertDTO> alerts;
    private AdminDashboardStorageDTO storage;
    private List<AdminDashboardReleaseSnapshotDTO> publishedReleases;
}