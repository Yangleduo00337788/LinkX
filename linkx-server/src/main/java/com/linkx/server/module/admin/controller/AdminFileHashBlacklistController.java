package com.linkx.server.module.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linkx.server.common.Result;
import com.linkx.server.entity.SysFileHashBlacklist;
import com.linkx.server.module.admin.dto.AddFileHashBlacklistRequest;
import com.linkx.server.module.admin.service.AdminFileHashBlacklistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/file-hash-blacklist")
@RequiredArgsConstructor
public class AdminFileHashBlacklistController {

    private final AdminFileHashBlacklistService service;

    @GetMapping
    public Result<Page<SysFileHashBlacklist>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword) {
        return Result.success(service.list(page, size, keyword));
    }

    @PostMapping
    public Result<Void> add(@Valid @RequestBody AddFileHashBlacklistRequest body) {
        service.add(body.getContentHash(), body.getReason());
        return Result.success();
    }

    @PutMapping("/{id}/enabled")
    public Result<Void> enabled(@PathVariable Long id, @RequestParam int enabled) {
        service.setEnabled(id, enabled);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        service.remove(id);
        return Result.success();
    }
}