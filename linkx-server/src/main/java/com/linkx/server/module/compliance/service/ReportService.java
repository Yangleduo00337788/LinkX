package com.linkx.server.module.compliance.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linkx.server.module.compliance.dto.CreateReportRequest;
import com.linkx.server.module.compliance.dto.HandleReportRequest;
import com.linkx.server.module.compliance.dto.ReportListItemDTO;
import com.linkx.server.module.admin.service.AdminAuditService;

public interface ReportService {

    Long submitReport(Long reporterUserId, CreateReportRequest request);

    Page<ReportListItemDTO> pageForAdmin(int page, int size, Integer status, String targetType);

    void handleReport(Long reportId, HandleReportRequest body, Long adminId, String adminUsername,
                      String clientIp, AdminAuditService audit);
}