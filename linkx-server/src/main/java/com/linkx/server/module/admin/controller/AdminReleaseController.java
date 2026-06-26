package com.linkx.server.module.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linkx.server.common.Result;
import com.linkx.server.module.admin.dto.AdminCreateReleaseRequest;
import com.linkx.server.module.admin.dto.AdminReleaseListItemDTO;
import com.linkx.server.module.admin.service.AdminAuditService;
import com.linkx.server.module.admin.service.AdminReleaseService;
import com.linkx.server.security.AdminPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/releases")
@RequiredArgsConstructor
public class AdminReleaseController {

    private final AdminReleaseService adminReleaseService;
    private final AdminAuditService adminAuditService;

    @GetMapping
    public Result<Page<AdminReleaseListItemDTO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String platform) {
        return Result.success(adminReleaseService.list(page, size, platform));
    }

    @PostMapping
    public Result<AdminReleaseListItemDTO> create(
            @Valid @RequestBody AdminCreateReleaseRequest body,
            @AuthenticationPrincipal AdminPrincipal principal,
            HttpServletRequest request) {
        return Result.success(adminReleaseService.create(body, principal.getAdminId(), principal.getUsername(),
                request.getRemoteAddr(), adminAuditService));
    }

    @PutMapping("/{id}/published")
    public Result<Void> setPublished(
            @PathVariable Long id,
            @RequestParam boolean published,
            @AuthenticationPrincipal AdminPrincipal principal,
            HttpServletRequest request) {
        adminReleaseService.setPublished(id, published, principal.getAdminId(), principal.getUsername(),
                request.getRemoteAddr(), adminAuditService);
        return Result.success();
    }
}