package com.linkx.server.module.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linkx.server.common.Result;
import com.linkx.server.module.admin.dto.AdminFriendListItemDTO;
import com.linkx.server.module.admin.dto.AdminUserDetailDTO;
import com.linkx.server.module.admin.dto.AdminUserListItemDTO;
import com.linkx.server.module.admin.service.AdminAuditService;
import com.linkx.server.module.admin.service.AdminUserService;
import com.linkx.server.security.AdminPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;
    private final AdminAuditService adminAuditService;

    @GetMapping
    public Result<Page<AdminUserListItemDTO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword) {
        return Result.success(adminUserService.listUsers(page, size, keyword));
    }

    @PutMapping("/{userId}/status")
    public Result<Void> updateStatus(
            @PathVariable Long userId,
            @RequestParam int status,
            @AuthenticationPrincipal AdminPrincipal principal,
            HttpServletRequest request) {
        adminUserService.setUserStatus(userId, status, principal.getAdminId(), principal.getUsername(),
                request.getRemoteAddr(), adminAuditService);
        return Result.success();
    }

    @GetMapping("/{userId}")
    public Result<AdminUserDetailDTO> detail(@PathVariable Long userId) {
        return Result.success(adminUserService.getUserDetail(userId));
    }

    @PostMapping("/{userId}/kick")
    public Result<Void> kick(
            @PathVariable Long userId,
            @AuthenticationPrincipal AdminPrincipal principal,
            HttpServletRequest request) {
        adminUserService.kickUser(userId, principal.getAdminId(), principal.getUsername(),
                request.getRemoteAddr(), adminAuditService);
        return Result.success();
    }

    @GetMapping("/{userId}/friends")
    public Result<Page<AdminFriendListItemDTO>> friends(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(adminUserService.listFriends(userId, page, size));
    }
}