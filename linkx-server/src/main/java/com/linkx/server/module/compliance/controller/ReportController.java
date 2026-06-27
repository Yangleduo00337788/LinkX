package com.linkx.server.module.compliance.controller;

import com.linkx.server.common.BusinessException;
import com.linkx.server.common.ErrorCode;
import com.linkx.server.common.Result;
import com.linkx.server.module.compliance.dto.CreateReportRequest;
import com.linkx.server.module.compliance.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public Result<Long> submit(@Valid @RequestBody CreateReportRequest request,
                               @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = resolveUserId(userDetails);
        return Result.success(reportService.submitReport(userId, request));
    }

    /** JWT 上下文中 {@link UserDetails#getUsername()} 为 userId 字符串，与全站约定一致。 */
    private static Long resolveUserId(UserDetails userDetails) {
        if (userDetails == null || userDetails.getUsername() == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        try {
            return Long.parseLong(userDetails.getUsername());
        } catch (NumberFormatException e) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
    }
}