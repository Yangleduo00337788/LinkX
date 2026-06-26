package com.linkx.server.module.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linkx.server.common.Result;
import com.linkx.server.module.admin.dto.AdminLoginLogListItemDTO;
import com.linkx.server.module.admin.service.AdminLoginLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/login-logs")
@RequiredArgsConstructor
public class AdminLoginLogController {

    private final AdminLoginLogService adminLoginLogService;

    @GetMapping
    public Result<Page<AdminLoginLogListItemDTO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer loginStatus) {
        return Result.success(adminLoginLogService.list(page, size, username, userId, loginStatus));
    }
}