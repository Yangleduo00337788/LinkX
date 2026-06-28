package com.linkx.server.module.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linkx.server.common.Result;
import com.linkx.server.module.admin.dto.AdminCreateReleaseRequest;
import com.linkx.server.module.admin.dto.AdminReleaseListItemDTO;
import com.linkx.server.config.LinkxAppProperties;
import com.linkx.server.module.admin.service.AdminAuditService;
import com.linkx.server.module.admin.service.AdminReleaseService;
import com.linkx.server.security.AdminPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/releases")
@RequiredArgsConstructor
public class AdminReleaseController {

    private final AdminReleaseService adminReleaseService;
    private final AdminAuditService adminAuditService;
    private final LinkxAppProperties linkxAppProperties;

    @GetMapping
    public Result<Page<AdminReleaseListItemDTO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String platform) {
        return Result.success(adminReleaseService.list(page, size, platform));
    }

    @PostMapping
    public Result<AdminReleaseListItemDTO> create(
            @Valid @RequestBody AdminCreateReleaseRequest body,
            @AuthenticationPrincipal AdminPrincipal principal,
            HttpServletRequest request) {
        return Result.success(adminReleaseService.create(body, principal.getAdminId(), principal.getUsername(),
                request.getRemoteAddr(), adminAuditService));
    }

    @PutMapping("/{id}/published")
    public Result<Void> setPublished(
            @PathVariable Long id,
            @RequestParam boolean published,
            @AuthenticationPrincipal AdminPrincipal principal,
            HttpServletRequest request) {
        adminReleaseService.setPublished(id, published, principal.getAdminId(), principal.getUsername(),
                request.getRemoteAddr(), adminAuditService);
        return Result.success();
    }

    /** 上传安装包到本地 releases 目录，返回可填入「下载地址」的 URL。 */
    @PostMapping("/upload")
    public Result<Map<String, String>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "win") String platform) throws IOException {
        if (file == null || file.isEmpty()) {
            return Result.fail(400, "请选择文件");
        }
        String plat = platform.trim().toLowerCase();
        String original = file.getOriginalFilename();
        String ext = "";
        if (StringUtils.hasText(original) && original.contains(".")) {
            ext = original.substring(original.lastIndexOf('.'));
        }
        String stored = plat + "-" + UUID.randomUUID() + ext;
        Path base = Paths.get(linkxAppProperties.getUpload().getPath(), "releases").toAbsolutePath().normalize();
        Files.createDirectories(base);
        Path target = base.resolve(stored);
        file.transferTo(target.toFile());
        String baseUrl = linkxAppProperties.getApiBaseUrl();
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        String downloadUrl = baseUrl + "/uploads/releases/" + stored;
        return Result.success(Map.of("downloadUrl", downloadUrl, "fileName", stored));
    }
}