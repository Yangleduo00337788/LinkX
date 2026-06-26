package com.linkx.server.module.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linkx.server.common.Result;
import com.linkx.server.module.admin.service.AdminAuditService;
import com.linkx.server.module.compliance.dto.HandleReportRequest;
import com.linkx.server.module.compliance.dto.ReportListItemDTO;
import com.linkx.server.module.compliance.service.ReportService;
import com.linkx.server.security.AdminPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
public class AdminReportController {

    private final ReportService reportService;
    private final AdminAuditService adminAuditService;

    @GetMapping
    public Result<Page<ReportListItemDTO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String targetType) {
        return Result.success(reportService.pageForAdmin(page, size, status, targetType));
    }

    @PutMapping("/{reportId}/handle")
    public Result<Void> handle(
            @PathVariable Long reportId,
            @Valid @RequestBody HandleReportRequest body,
            @AuthenticationPrincipal AdminPrincipal principal,
            HttpServletRequest request) {
        reportService.handleReport(reportId, body, principal.getAdminId(), principal.getUsername(),
                request.getRemoteAddr(), adminAuditService);
        return Result.success();
    }
}