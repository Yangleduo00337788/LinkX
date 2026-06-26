package com.linkx.server.module.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linkx.server.common.Result;
import com.linkx.server.module.admin.dto.AdminMessageListItemDTO;
import com.linkx.server.module.admin.service.AdminAuditService;
import com.linkx.server.module.admin.service.AdminMessageService;
import com.linkx.server.security.AdminPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/messages")
@RequiredArgsConstructor
public class AdminMessageController {

    private final AdminMessageService adminMessageService;
    private final AdminAuditService adminAuditService;

    @GetMapping
    public Result<Page<AdminMessageListItemDTO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long sessionId,
            @RequestParam(required = false) Long fromUserId) {
        return Result.success(adminMessageService.list(page, size, sessionId, fromUserId));
    }

    @DeleteMapping("/{messageId}")
    public Result<Void> delete(
            @PathVariable Long messageId,
            @AuthenticationPrincipal AdminPrincipal principal,
            HttpServletRequest request) {
        adminMessageService.delete(messageId, principal.getAdminId(), principal.getUsername(),
                request.getRemoteAddr(), adminAuditService);
        return Result.success();
    }
}