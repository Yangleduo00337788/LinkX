package com.linkx.server.module.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linkx.server.common.Result;
import com.linkx.server.module.admin.dto.AdminGroupListItemDTO;
import com.linkx.server.module.group.dto.GroupDetailDTO;
import com.linkx.server.module.admin.service.AdminAuditService;
import com.linkx.server.module.admin.service.AdminGroupService;
import com.linkx.server.security.AdminPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/groups")
@RequiredArgsConstructor
public class AdminGroupController {

    private final AdminGroupService adminGroupService;
    private final AdminAuditService adminAuditService;

    @GetMapping
    public Result<Page<AdminGroupListItemDTO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword) {
        return Result.success(adminGroupService.list(page, size, keyword));
    }

    @GetMapping("/{groupId}")
    public Result<GroupDetailDTO> detail(@PathVariable Long groupId) {
        return Result.success(adminGroupService.getDetail(groupId));
    }

    @PutMapping("/{groupId}/members/{userId}/mute")
    public Result<Void> mute(
            @PathVariable Long groupId,
            @PathVariable Long userId,
            @RequestParam(defaultValue = "60") Integer muteMinutes,
            @AuthenticationPrincipal AdminPrincipal principal,
            HttpServletRequest request) {
        adminGroupService.muteMember(groupId, userId, muteMinutes, principal.getAdminId(),
                principal.getUsername(), request.getRemoteAddr(), adminAuditService);
        return Result.success();
    }

    @PutMapping("/{groupId}/members/{userId}/unmute")
    public Result<Void> unmute(
            @PathVariable Long groupId,
            @PathVariable Long userId,
            @AuthenticationPrincipal AdminPrincipal principal,
            HttpServletRequest request) {
        adminGroupService.unmuteMember(groupId, userId, principal.getAdminId(), principal.getUsername(),
                request.getRemoteAddr(), adminAuditService);
        return Result.success();
    }

    @DeleteMapping("/{groupId}")
    public Result<Void> dissolve(
            @PathVariable Long groupId,
            @AuthenticationPrincipal AdminPrincipal principal,
            HttpServletRequest request) {
        adminGroupService.dissolve(groupId, principal.getAdminId(), principal.getUsername(),
                request.getRemoteAddr(), adminAuditService);
        return Result.success();
    }
}