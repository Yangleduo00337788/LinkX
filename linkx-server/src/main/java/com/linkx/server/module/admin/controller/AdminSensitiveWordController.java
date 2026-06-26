package com.linkx.server.module.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linkx.server.common.Result;
import com.linkx.server.entity.SysSensitiveHitLog;
import com.linkx.server.entity.SysSensitiveWord;
import com.linkx.server.module.compliance.service.SensitiveWordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/sensitive-words")
@RequiredArgsConstructor
public class AdminSensitiveWordController {

    private final SensitiveWordService sensitiveWordService;

    @GetMapping
    public Result<Page<SysSensitiveWord>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer enabled) {
        return Result.success(sensitiveWordService.pageWords(page, size, keyword, enabled));
    }

    @PostMapping
    public Result<Void> create(@RequestBody SysSensitiveWord word) {
        sensitiveWordService.createWord(word);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody SysSensitiveWord word) {
        word.setId(id);
        sensitiveWordService.updateWord(word);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        sensitiveWordService.deleteWord(id);
        return Result.success();
    }

    @PostMapping("/refresh-cache")
    public Result<Void> refreshCache() {
        sensitiveWordService.refreshCache();
        return Result.success();
    }

    @GetMapping("/hits")
    public Result<Page<SysSensitiveHitLog>> hits(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long userId) {
        return Result.success(sensitiveWordService.pageHits(page, size, userId));
    }
}