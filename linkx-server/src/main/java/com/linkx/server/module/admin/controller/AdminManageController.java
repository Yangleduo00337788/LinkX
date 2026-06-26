package com.linkx.server.module.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linkx.server.common.Result;
import com.linkx.server.module.admin.dto.AdminManageListItemDTO;
import com.linkx.server.module.admin.dto.CreateAdminRequest;
import com.linkx.server.module.admin.service.AdminManageService;
import com.linkx.server.security.AdminPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/admins")
@RequiredArgsConstructor
public class AdminManageController {

    private final AdminManageService adminManageService;

    @GetMapping
    public Result<Page<AdminManageListItemDTO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword) {
        return Result.success(adminManageService.list(page, size, keyword));
    }

    @PostMapping
    public Result<Void> create(
            @Valid @RequestBody CreateAdminRequest body,
            @AuthenticationPrincipal AdminPrincipal principal,
            HttpServletRequest request) {
        adminManageService.create(body, principal.getAdminId(), principal.getUsername(), request.getRemoteAddr());
        return Result.success();
    }

    @PutMapping("/{adminId}/status")
    public Result<Void> status(
            @PathVariable Long adminId,
            @RequestParam int status,
            @AuthenticationPrincipal AdminPrincipal principal,
            HttpServletRequest request) {
        adminManageService.setStatus(adminId, status, principal.getAdminId(), principal.getUsername(),
                request.getRemoteAddr());
        return Result.success();
    }

    @PutMapping("/{adminId}/password")
    public Result<Void> resetPassword(
            @PathVariable Long adminId,
            @RequestParam String newPassword,
            @AuthenticationPrincipal AdminPrincipal principal,
            HttpServletRequest request) {
        adminManageService.resetPassword(adminId, newPassword, principal.getAdminId(), principal.getUsername(),
                request.getRemoteAddr());
        return Result.success();
    }
}