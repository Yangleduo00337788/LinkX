package com.linkx.server.module.admin.controller;

import com.linkx.server.common.Result;
import com.linkx.server.entity.SysRuntimeConfig;
import com.linkx.server.module.admin.dto.RuntimeConfigUpdateRequest;
import com.linkx.server.module.admin.service.RuntimeConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/runtime-config")
@RequiredArgsConstructor
public class AdminRuntimeConfigController {

    private final RuntimeConfigService runtimeConfigService;

    @GetMapping
    public Result<List<SysRuntimeConfig>> list() {
        return Result.success(runtimeConfigService.listAll());
    }

    @PutMapping
    public Result<Void> upsert(@Valid @RequestBody RuntimeConfigUpdateRequest body) {
        runtimeConfigService.upsert(body.getConfigKey().trim(), body.getConfigValue().trim(), body.getDescription());
        return Result.success();
    }

    @GetMapping("/presets")
    public Result<Map<String, String>> presets() {
        return Result.success(runtimeConfigService.listPublicSettings());
    }
}