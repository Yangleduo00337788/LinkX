package com.linkx.server.module.compliance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linkx.server.common.BusinessException;
import com.linkx.server.common.ErrorCode;
import com.linkx.server.entity.SysReport;
import com.linkx.server.entity.SysUser;
import com.linkx.server.mapper.SysReportMapper;
import com.linkx.server.mapper.SysUserMapper;
import com.linkx.server.module.admin.service.AdminAuditService;
import com.linkx.server.module.admin.service.AdminMessageService;
import com.linkx.server.module.admin.service.AdminUserService;
import com.linkx.server.module.compliance.dto.CreateReportRequest;
import com.linkx.server.module.compliance.dto.HandleReportRequest;
import com.linkx.server.module.compliance.dto.ReportListItemDTO;
import com.linkx.server.module.compliance.service.ReportService;
import com.linkx.server.module.notification.service.UserNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private static final Set<String> TARGET_TYPES = Set.of("USER", "MESSAGE", "GROUP", "FILE");
    private static final int STATUS_PENDING = 0;
    private static final int STATUS_DISMISSED = 3;

    private final SysReportMapper reportMapper;
    private final SysUserMapper userMapper;
    private final UserNotificationService userNotificationService;
    private final AdminMessageService adminMessageService;
    private final AdminUserService adminUserService;

    @Override
    @Transactional
    public Long submitReport(Long reporterUserId, CreateReportRequest request) {
        String type = request.getTargetType().trim().toUpperCase();
        if (!TARGET_TYPES.contains(type)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "不支持的举报类型");
        }
        SysReport report = new SysReport();
        report.setReporterUserId(reporterUserId);
        report.setTargetType(type);
        report.setTargetId(request.getTargetId().trim());
        report.setReasonCategory(request.getReasonCategory().trim());
        report.setReasonDetail(StringUtils.hasText(request.getReasonDetail()) ? request.getReasonDetail().trim() : null);
        report.setStatus(STATUS_PENDING);
        report.setNotifyReporter(1);
        report.setReporterNotified(0);
        reportMapper.insert(report);
        return report.getId();
    }

    @Override
    public Page<ReportListItemDTO> pageForAdmin(int page, int size, Integer status, String targetType) {
        LambdaQueryWrapper<SysReport> w = new LambdaQueryWrapper<>();
        if (status != null) {
            w.eq(SysReport::getStatus, status);
        }
        if (StringUtils.hasText(targetType)) {
            w.eq(SysReport::getTargetType, targetType.trim().toUpperCase());
        }
        w.orderByDesc(SysReport::getCreateTime);
        Page<SysReport> raw = reportMapper.selectPage(new Page<>(page, size), w);
        Map<Long, String> usernames = loadUsernames(raw.getRecords().stream()
                .map(SysReport::getReporterUserId)
                .collect(Collectors.toSet()));
        Page<ReportListItemDTO> result = new Page<>(raw.getCurrent(), raw.getSize(), raw.getTotal());
        result.setRecords(raw.getRecords().stream().map(r -> ReportListItemDTO.builder()
                .id(r.getId())
                .reporterUserId(r.getReporterUserId())
                .reporterUsername(usernames.get(r.getReporterUserId()))
                .targetType(r.getTargetType())
                .targetId(r.getTargetId())
                .reasonCategory(r.getReasonCategory())
                .reasonDetail(r.getReasonDetail())
                .status(r.getStatus())
                .resolution(r.getResolution())
                .resolutionNote(r.getResolutionNote())
                .handlerAdminId(r.getHandlerAdminId())
                .handledTime(r.getHandledTime())
                .createTime(r.getCreateTime())
                .build()).toList());
        return result;
    }

    @Override
    public Page<ReportListItemDTO> pageForReporter(Long reporterUserId, int page, int size) {
        LambdaQueryWrapper<SysReport> w = new LambdaQueryWrapper<>();
        w.eq(SysReport::getReporterUserId, reporterUserId).orderByDesc(SysReport::getCreateTime);
        Page<SysReport> raw = reportMapper.selectPage(new Page<>(page, size), w);
        Page<ReportListItemDTO> result = new Page<>(raw.getCurrent(), raw.getSize(), raw.getTotal());
        result.setRecords(raw.getRecords().stream().map(r -> ReportListItemDTO.builder()
                .id(r.getId())
                .reporterUserId(r.getReporterUserId())
                .targetType(r.getTargetType())
                .targetId(r.getTargetId())
                .reasonCategory(r.getReasonCategory())
                .reasonDetail(r.getReasonDetail())
                .status(r.getStatus())
                .resolution(r.getResolution())
                .resolutionNote(r.getResolutionNote())
                .handlerAdminId(r.getHandlerAdminId())
                .handledTime(r.getHandledTime())
                .createTime(r.getCreateTime())
                .build()).toList());
        return result;
    }

    @Override
    @Transactional
    public void handleReport(Long reportId, HandleReportRequest body, Long adminId, String adminUsername,
                             String clientIp, AdminAuditService audit) {
        SysReport report = reportMapper.selectById(reportId);
        if (report == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        String resolution = body.getResolution();
        if (StringUtils.hasText(resolution)) {
            applyResolution(report, resolution, adminId, adminUsername, clientIp, audit);
        }
        report.setStatus(body.getStatus());
        report.setResolution(resolution);
        report.setResolutionNote(body.getResolutionNote());
        report.setHandlerAdminId(adminId);
        report.setHandledTime(LocalDateTime.now());
        reportMapper.updateById(report);

        boolean notify = body.getNotifyReporter() == null || Boolean.TRUE.equals(body.getNotifyReporter());
        if (notify && report.getReporterNotified() != null && report.getReporterNotified() == 0) {
            String title = body.getStatus() != null && body.getStatus() == STATUS_DISMISSED
                    ? "举报处理结果"
                    : "举报已处理";
            String content = StringUtils.hasText(body.getResolutionNote())
                    ? body.getResolutionNote()
                    : "您的举报单 #" + reportId + " 已处理，感谢反馈。";
            userNotificationService.notifyUser(report.getReporterUserId(), title, content, "REPORT_RESULT",
                    String.valueOf(reportId));
            report.setReporterNotified(1);
            reportMapper.updateById(report);
        }
        audit.record(adminId, adminUsername, "REPORT_HANDLE", "report", String.valueOf(reportId),
                "status=" + body.getStatus() + ",resolution=" + resolution, clientIp);
    }

    private void applyResolution(SysReport report, String resolution, Long adminId, String adminUsername,
                                 String clientIp, AdminAuditService audit) {
        switch (resolution.toUpperCase()) {
            case "DELETE_CONTENT" -> {
                if ("MESSAGE".equals(report.getTargetType())) {
                    adminMessageService.delete(Long.parseLong(report.getTargetId()), adminId, adminUsername, clientIp, audit);
                }
            }
            case "BAN_USER" -> {
                if ("USER".equals(report.getTargetType())) {
                    adminUserService.setUserStatus(Long.parseLong(report.getTargetId()), 0, adminId, adminUsername,
                            clientIp, audit);
                }
            }
            case "WARN", "DISMISS", "BAN_GROUP" -> {
                // 警告/驳回仅记录；封群在 P1 群治理中扩展
            }
            default -> {
            }
        }
    }

    private Map<Long, String> loadUsernames(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }
        return userMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(SysUser::getId, SysUser::getUsername, (a, b) -> a));
    }
}