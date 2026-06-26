package com.linkx.server.module.admin.dto;

import lombok.Builder;
import lombok.Data;

/** 新增指标环比（较昨日 / 较上周同日） */
@Data
@Builder
public class AdminDashboardCompareDTO {
    private long newUsersToday;
    private long newUsersYesterday;
    private long newUsersSameDayLastWeek;
    /** 较昨日变化率，null 表示昨日为 0 无法计算 */
    private Double newUsersDayOverDayRate;
    private Double newUsersWeekOverWeekRate;

    private long newMessagesToday;
    private long newMessagesYesterday;
    private long newMessagesSameDayLastWeek;
    private Double newMessagesDayOverDayRate;
    private Double newMessagesWeekOverWeekRate;
}