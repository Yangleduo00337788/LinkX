package com.linkx.server.module.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.linkx.server.config.LinkxMinioProperties;
import com.linkx.server.module.chat.ws.ChatSocketSessionRegistry;
import com.linkx.server.entity.SysAppRelease;
import com.linkx.server.entity.SysAuditLog;
import com.linkx.server.mapper.ImGroupInfoMapper;
import com.linkx.server.mapper.ImGroupRequestMapper;
import com.linkx.server.mapper.ImMessageMapper;
import com.linkx.server.mapper.SysAppReleaseMapper;
import com.linkx.server.mapper.SysAuditLogMapper;
import com.linkx.server.mapper.SysFileMapper;
import com.linkx.server.mapper.SysFriendRequestMapper;
import com.linkx.server.mapper.SysUserMapper;
import com.linkx.server.module.admin.dto.AdminAuditLogListItemDTO;
import com.linkx.server.module.admin.dto.AdminDashboardAlertDTO;
import com.linkx.server.module.admin.dto.AdminDashboardCompareDTO;
import com.linkx.server.module.admin.dto.AdminDashboardGroupRankDTO;
import com.linkx.server.module.admin.dto.AdminDashboardHealthDTO;
import com.linkx.server.module.admin.dto.AdminDashboardMessageTypeSliceDTO;
import com.linkx.server.module.admin.dto.AdminDashboardReleaseSnapshotDTO;
import com.linkx.server.module.admin.dto.AdminDashboardStatsDTO;
import com.linkx.server.module.admin.dto.AdminDashboardStorageDTO;
import com.linkx.server.module.admin.dto.AdminDashboardTrendPointDTO;
import com.linkx.server.entity.SysFriendRequest;
import com.linkx.server.entity.ImGroupRequest;

import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private static final int DEFAULT_TREND_DAYS = 7;
    private static final int MAX_TREND_DAYS = 30;

    private final SysUserMapper userMapper;
    private final ImGroupInfoMapper groupInfoMapper;
    private final ImMessageMapper messageMapper;
    private final SysFileMapper fileMapper;
    private final SysFriendRequestMapper friendRequestMapper;
    private final ImGroupRequestMapper groupRequestMapper;
    private final SysAuditLogMapper auditLogMapper;
    private final SysAppReleaseMapper appReleaseMapper;
    private final JdbcTemplate jdbcTemplate;
    private final ChatSocketSessionRegistry chatSocketSessionRegistry;
    private final RedisConnectionFactory redisConnectionFactory;
    private final ObjectProvider<MinioClient> minioClientProvider;
    private final LinkxMinioProperties linkxMinioProperties;

    public AdminDashboardStatsDTO stats(int trendDays) {
        int days = Math.min(Math.max(trendDays, 7), MAX_TREND_DAYS);
        if (days != 7 && days != 30) {
            days = DEFAULT_TREND_DAYS;
        }
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(days - 1L);

        long todayUsers = countUsersOn(today);
        long todayMessages = countMessagesOn(today);
        LocalDate yesterday = today.minusDays(1);
        LocalDate lastWeekSame = today.minusDays(7);

        return AdminDashboardStatsDTO.builder()
                .userCount(countActiveUsers())
                .groupCount(groupInfoMapper.selectCount(null))
                .messageCount(messageMapper.selectCount(null))
                .fileCount(fileMapper.selectCount(null))
                .todayNewUsers(todayUsers)
                .todayNewMessages(todayMessages)
                .todayLoginUserCount(countDistinctLoginUsersOn(today))
                .todayActiveMessageUserCount(countDistinctMessageSendersOn(today))
                .disabledUserCount(countDisabledUsers())
                .blacklistCount(countBlacklist())
                .todayKickCount(countKicksOn(today))
                .pendingFriendRequests(countPendingFriendRequests())
                .pendingGroupRequests(countPendingGroupRequests())
                .trendDays(days)
                .trend(buildTrend(start, today))
                .compare(buildCompare(todayUsers, todayMessages, yesterday, lastWeekSame))
                .messageTypeBreakdown(messageTypeBreakdown())
                .topGroupsByMembers(topGroupsByMembers(5))
                .topGroupsByMessages7d(topGroupsByMessages7d(5))
                .recentAuditLogs(recentAuditLogs(10))
                .onlineUserCount(chatSocketSessionRegistry.getOnlineUserCount())
                .health(checkHealth())
                .alerts(buildAlerts(todayUsers, todayMessages, yesterday))
                .storage(storageSummary())
                .publishedReleases(publishedReleaseSnapshots())
                .build();
    }

    private long countActiveUsers() {
        Long n = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sys_user WHERE deleted = 0", Long.class);
        return n != null ? n : 0L;
    }

    private long countDisabledUsers() {
        Long n = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sys_user WHERE deleted = 0 AND status = 0", Long.class);
        return n != null ? n : 0L;
    }

    private long countBlacklist() {
        Long n = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sys_blacklist", Long.class);
        return n != null ? n : 0L;
    }

    private long countPendingFriendRequests() {
        return friendRequestMapper.selectCount(
                new LambdaQueryWrapper<SysFriendRequest>().eq(SysFriendRequest::getStatus, 0));
    }

    private long countPendingGroupRequests() {
        return groupRequestMapper.selectCount(
                new LambdaQueryWrapper<ImGroupRequest>().eq(ImGroupRequest::getStatus, 0));
    }

    private long countKicksOn(LocalDate day) {
        Long n = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sys_audit_log WHERE action = 'USER_KICK' AND create_time >= ? AND create_time < ?",
                Long.class,
                day.atStartOfDay(),
                day.plusDays(1).atStartOfDay());
        return n != null ? n : 0L;
    }

    private long countDistinctLoginUsersOn(LocalDate day) {
        Long n = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(DISTINCT user_id) FROM sys_login_log
                        WHERE user_id IS NOT NULL AND login_status = 1
                        AND create_time >= ? AND create_time < ?
                        """,
                Long.class,
                day.atStartOfDay(),
                day.plusDays(1).atStartOfDay());
        return n != null ? n : 0L;
    }

    private long countDistinctMessageSendersOn(LocalDate day) {
        Long n = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(DISTINCT from_user_id) FROM im_message
                        WHERE create_time >= ? AND create_time < ?
                        """,
                Long.class,
                day.atStartOfDay(),
                day.plusDays(1).atStartOfDay());
        return n != null ? n : 0L;
    }

    private AdminDashboardCompareDTO buildCompare(
            long usersToday, long msgToday, LocalDate yesterday, LocalDate lastWeekSame) {
        long usersYesterday = countUsersOn(yesterday);
        long usersLastWeek = countUsersOn(lastWeekSame);
        long msgYesterday = countMessagesOn(yesterday);
        long msgLastWeek = countMessagesOn(lastWeekSame);
        return AdminDashboardCompareDTO.builder()
                .newUsersToday(usersToday)
                .newUsersYesterday(usersYesterday)
                .newUsersSameDayLastWeek(usersLastWeek)
                .newUsersDayOverDayRate(rate(usersToday, usersYesterday))
                .newUsersWeekOverWeekRate(rate(usersToday, usersLastWeek))
                .newMessagesToday(msgToday)
                .newMessagesYesterday(msgYesterday)
                .newMessagesSameDayLastWeek(msgLastWeek)
                .newMessagesDayOverDayRate(rate(msgToday, msgYesterday))
                .newMessagesWeekOverWeekRate(rate(msgToday, msgLastWeek))
                .build();
    }

    private static Double rate(long current, long base) {
        if (base == 0) {
            return current == 0 ? 0.0 : null;
        }
        return (current - base) * 100.0 / base;
    }

    private List<AdminDashboardTrendPointDTO> buildTrend(LocalDate start, LocalDate end) {
        Map<LocalDate, Long> usersByDay = dailyUserCounts(start, end);
        Map<LocalDate, Long> messagesByDay = dailyMessageCounts(start, end);
        List<AdminDashboardTrendPointDTO> list = new ArrayList<>();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            list.add(AdminDashboardTrendPointDTO.builder()
                    .date(d.toString())
                    .newUsers(usersByDay.getOrDefault(d, 0L))
                    .newMessages(messagesByDay.getOrDefault(d, 0L))
                    .build());
        }
        return list;
    }

    private Map<LocalDate, Long> dailyUserCounts(LocalDate start, LocalDate end) {
        String sql = """
                SELECT DATE(create_time) AS d, COUNT(*) AS c
                FROM sys_user
                WHERE deleted = 0 AND create_time >= ? AND create_time < ?
                GROUP BY DATE(create_time)
                """;
        return mapDateCounts(sql, start, end.plusDays(1));
    }

    private Map<LocalDate, Long> dailyMessageCounts(LocalDate start, LocalDate end) {
        String sql = """
                SELECT DATE(create_time) AS d, COUNT(*) AS c
                FROM im_message
                WHERE create_time >= ? AND create_time < ?
                GROUP BY DATE(create_time)
                """;
        return mapDateCounts(sql, start, end.plusDays(1));
    }

    private Map<LocalDate, Long> mapDateCounts(String sql, LocalDate startInclusive, LocalDate endExclusive) {
        Map<LocalDate, Long> map = new HashMap<>();
        jdbcTemplate.query(sql,
                rs -> {
                    LocalDate d = rs.getDate("d").toLocalDate();
                    map.put(d, rs.getLong("c"));
                },
                startInclusive.atStartOfDay(),
                endExclusive.atStartOfDay());
        return map;
    }

    private long countUsersOn(LocalDate day) {
        Long n = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sys_user WHERE deleted = 0 AND create_time >= ? AND create_time < ?",
                Long.class,
                day.atStartOfDay(),
                day.plusDays(1).atStartOfDay());
        return n != null ? n : 0L;
    }

    private long countMessagesOn(LocalDate day) {
        Long n = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM im_message WHERE create_time >= ? AND create_time < ?",
                Long.class,
                day.atStartOfDay(),
                day.plusDays(1).atStartOfDay());
        return n != null ? n : 0L;
    }

    private List<AdminDashboardMessageTypeSliceDTO> messageTypeBreakdown() {
        String sql = "SELECT msg_type, COUNT(*) AS c FROM im_message GROUP BY msg_type ORDER BY msg_type";
        List<AdminDashboardMessageTypeSliceDTO> list = new ArrayList<>();
        jdbcTemplate.query(sql, rs -> {
            int type = rs.getInt("msg_type");
            list.add(AdminDashboardMessageTypeSliceDTO.builder()
                    .msgType(type)
                    .label(messageTypeLabel(type))
                    .count(rs.getLong("c"))
                    .build());
        });
        return list;
    }

    private static String messageTypeLabel(int type) {
        return switch (type) {
            case 1 -> "图片";
            case 2 -> "文件";
            case 3 -> "系统";
            default -> "文本";
        };
    }

    private List<AdminDashboardGroupRankDTO> topGroupsByMembers(int limit) {
        String sql = """
                SELECT g.id, g.group_name, COUNT(m.id) AS mc
                FROM im_group_info g
                LEFT JOIN im_group_member m ON g.id = m.group_id
                WHERE g.deleted = 0
                GROUP BY g.id, g.group_name
                ORDER BY mc DESC
                LIMIT ?
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> AdminDashboardGroupRankDTO.builder()
                .groupId(rs.getLong("id"))
                .groupName(rs.getString("group_name"))
                .memberCount(rs.getLong("mc"))
                .messageCount7d(0)
                .build(), limit);
    }

    private List<AdminDashboardGroupRankDTO> topGroupsByMessages7d(int limit) {
        LocalDateTime since = LocalDate.now().minusDays(6).atStartOfDay();
        String sql = """
                SELECT s.target_id AS gid, g.group_name, COUNT(msg.id) AS mc
                FROM im_message msg
                INNER JOIN im_session s ON msg.session_id = s.id AND s.session_type = 2
                LEFT JOIN im_group_info g ON g.id = s.target_id AND g.deleted = 0
                WHERE msg.create_time >= ?
                GROUP BY s.target_id, g.group_name
                ORDER BY mc DESC
                LIMIT ?
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> AdminDashboardGroupRankDTO.builder()
                .groupId(rs.getLong("gid"))
                .groupName(rs.getString("group_name") != null ? rs.getString("group_name") : "—")
                .memberCount(0)
                .messageCount7d(rs.getLong("mc"))
                .build(), since, limit);
    }

    private List<AdminAuditLogListItemDTO> recentAuditLogs(int limit) {
        List<SysAuditLog> logs = auditLogMapper.selectList(
                new LambdaQueryWrapper<SysAuditLog>()
                        .orderByDesc(SysAuditLog::getCreateTime)
                        .last("LIMIT " + limit));
        return logs.stream()
                .map(l -> AdminAuditLogListItemDTO.builder()
                        .id(l.getId())
                        .adminUsername(l.getAdminUsername())
                        .action(l.getAction())
                        .targetType(l.getTargetType())
                        .targetId(l.getTargetId())
                        .detail(l.getDetail())
                        .clientIp(l.getClientIp())
                        .createTime(l.getCreateTime())
                        .build())
                .toList();
    }

    private AdminDashboardStorageDTO storageSummary() {
        Long bytes = jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(file_size), 0) FROM sys_file", Long.class);
        long count = fileMapper.selectCount(null);
        return AdminDashboardStorageDTO.builder()
                .fileCount(count)
                .totalBytes(bytes != null ? bytes : 0L)
                .build();
    }

    private List<AdminDashboardReleaseSnapshotDTO> publishedReleaseSnapshots() {
        List<SysAppRelease> all = appReleaseMapper.selectList(
                new LambdaQueryWrapper<SysAppRelease>()
                        .eq(SysAppRelease::getPublished, 1)
                        .orderByDesc(SysAppRelease::getCreateTime));
        Map<String, AdminDashboardReleaseSnapshotDTO> latest = new LinkedHashMap<>();
        for (SysAppRelease r : all) {
            latest.putIfAbsent(r.getPlatform(), AdminDashboardReleaseSnapshotDTO.builder()
                    .platform(r.getPlatform())
                    .version(r.getVersion())
                    .published(r.getPublished())
                    .build());
        }
        return new ArrayList<>(latest.values());
    }

    private AdminDashboardHealthDTO checkHealth() {
        String mysql = "UP";
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
        } catch (Exception e) {
            mysql = "DOWN";
        }
        String redis = "UP";
        try {
            redisConnectionFactory.getConnection().ping();
        } catch (Exception e) {
            redis = "DOWN";
        }
        String minio = "N/A";
        if (linkxMinioProperties.isEnabled()) {
            minio = "DOWN";
            MinioClient client = minioClientProvider.getIfAvailable();
            if (client != null) {
                try {
                    client.listBuckets();
                    minio = "UP";
                } catch (Exception ignored) {
                    minio = "DOWN";
                }
            }
        }
        return AdminDashboardHealthDTO.builder()
                .mysql(mysql)
                .redis(redis)
                .minio(minio)
                .build();
    }

    private List<AdminDashboardAlertDTO> buildAlerts(long usersToday, long msgToday, LocalDate yesterday) {
        List<AdminDashboardAlertDTO> alerts = new ArrayList<>();
        long pendingFr = countPendingFriendRequests();
        long pendingGr = countPendingGroupRequests();
        if (pendingFr >= 20) {
            alerts.add(AdminDashboardAlertDTO.builder()
                    .level("WARN")
                    .code("PENDING_FRIEND_REQUESTS")
                    .message("待处理好友申请较多：" + pendingFr + " 条")
                    .build());
        }
        if (pendingGr >= 20) {
            alerts.add(AdminDashboardAlertDTO.builder()
                    .level("WARN")
                    .code("PENDING_GROUP_REQUESTS")
                    .message("待处理入群申请较多：" + pendingGr + " 条")
                    .build());
        }
        long msgYesterday = countMessagesOn(yesterday);
        if (msgYesterday > 0 && msgToday > msgYesterday * 3) {
            alerts.add(AdminDashboardAlertDTO.builder()
                    .level("INFO")
                    .code("MESSAGE_SPIKE")
                    .message("今日消息量明显高于昨日，请关注异常刷屏或活动峰值")
                    .build());
        }
        AdminDashboardHealthDTO health = checkHealth();
        if ("DOWN".equals(health.getMysql()) || "DOWN".equals(health.getRedis())) {
            alerts.add(AdminDashboardAlertDTO.builder()
                    .level("ERROR")
                    .code("INFRA_DOWN")
                    .message("核心依赖异常：MySQL=" + health.getMysql() + " Redis=" + health.getRedis())
                    .build());
        }
        if (alerts.isEmpty()) {
            alerts.add(AdminDashboardAlertDTO.builder()
                    .level("INFO")
                    .code("OK")
                    .message("暂无需要处理的告警")
                    .build());
        }
        return alerts;
    }
}