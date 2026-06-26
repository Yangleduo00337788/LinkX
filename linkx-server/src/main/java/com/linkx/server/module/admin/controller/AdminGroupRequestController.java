package com.linkx.server.module.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linkx.server.common.Result;
import com.linkx.server.module.admin.dto.AdminGroupRequestListItemDTO;
import com.linkx.server.module.admin.service.AdminGroupRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/group-requests")
@RequiredArgsConstructor
public class AdminGroupRequestController {

    private final AdminGroupRequestService adminGroupRequestService;

    @GetMapping
    public Result<Page<AdminGroupRequestListItemDTO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long groupId) {
        return Result.success(adminGroupRequestService.list(page, size, status, groupId));
    }
}