package com.linkx.server.module.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linkx.server.common.Result;
import com.linkx.server.module.admin.dto.AdminFileListItemDTO;
import com.linkx.server.module.admin.service.AdminAuditService;
import com.linkx.server.module.admin.service.AdminFileService;
import com.linkx.server.security.AdminPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/files")
@RequiredArgsConstructor
public class AdminFileController {

    private final AdminFileService adminFileService;
    private final AdminAuditService adminAuditService;

    @GetMapping
    public Result<Page<AdminFileListItemDTO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long userId) {
        return Result.success(adminFileService.list(page, size, userId));
    }

    @DeleteMapping("/{fileId}")
    public Result<Void> delete(
            @PathVariable Long fileId,
            @AuthenticationPrincipal AdminPrincipal principal,
            HttpServletRequest request) {
        adminFileService.delete(fileId, principal.getAdminId(), principal.getUsername(),
                request.getRemoteAddr(), adminAuditService);
        return Result.success();
    }
}