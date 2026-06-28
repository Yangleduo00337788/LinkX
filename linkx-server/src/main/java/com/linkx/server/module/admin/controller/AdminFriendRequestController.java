package com.linkx.server.module.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linkx.server.common.Result;
import com.linkx.server.module.admin.dto.AdminFriendRequestListItemDTO;
import com.linkx.server.module.admin.service.AdminFriendRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/friend-requests")
@RequiredArgsConstructor
public class AdminFriendRequestController {

    private final AdminFriendRequestService adminFriendRequestService;

    @GetMapping
    public Result<Page<AdminFriendRequestListItemDTO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer status) {
        return Result.success(adminFriendRequestService.list(page, size, status));
    }

    @PutMapping("/{id}/accept")
    public Result<Void> accept(@PathVariable Long id) {
        adminFriendRequestService.accept(id);
        return Result.success();
    }

    @PutMapping("/{id}/reject")
    public Result<Void> reject(@PathVariable Long id) {
        adminFriendRequestService.reject(id);
        return Result.success();
    }
}