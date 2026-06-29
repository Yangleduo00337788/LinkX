package com.linkx.server.module.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linkx.server.common.Result;
import com.linkx.server.entity.SysFileHashBlacklist;
import com.linkx.server.module.admin.service.AdminFileHashBlacklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
    public Result<Void> create(@RequestBody Map<String, Object> body) {
        String hash = String.valueOf(body.get("contentHash"));
        String reason = body.get("reason") != null ? String.valueOf(body.get("reason")) : null;
        Integer enabled = body.get("enabled") instanceof Number n ? n.intValue() : 1;
        service.create(hash, reason, enabled);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String reason = body.get("reason") != null ? String.valueOf(body.get("reason")) : null;
        Integer enabled = body.get("enabled") instanceof Number n ? n.intValue() : null;
        service.update(id, reason, enabled);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return Result.success();
    }
}