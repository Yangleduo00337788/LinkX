package com.linkx.server.module.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linkx.server.common.BusinessException;
import com.linkx.server.common.ErrorCode;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/releases")
@RequiredArgsConstructor
public class AdminReleaseController {

    private final AdminReleaseService adminReleaseService;
    private final AdminAuditService adminAuditService;
    private final LinkxAppProperties appProperties;

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

    /** 上传安装包到本地 uploads/releases/，返回可填入 downloadUrl 的相对路径 */
    @PostMapping("/upload")
    public Result<Map<String, String>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "win") String platform) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "文件为空");
        }
        String safePlatform = platform.trim().toLowerCase().replaceAll("[^a-z0-9_-]", "");
        if (safePlatform.isEmpty()) {
            safePlatform = "win";
        }
        String original = file.getOriginalFilename() != null ? file.getOriginalFilename() : "package.bin";
        String ext = "";
        int dot = original.lastIndexOf('.');
        if (dot >= 0) {
            ext = original.substring(dot).replaceAll("[^a-zA-Z0-9.]", "");
        }
        String stored = safePlatform + "/" + UUID.randomUUID() + ext;
        Path base = Path.of(appProperties.getUpload().getPath(), "releases");
        Files.createDirectories(base.resolve(safePlatform));
        Path target = base.resolve(stored.replace("/", java.io.File.separator));
        Files.write(target, file.getBytes());
        String urlPrefix = appProperties.getUpload().getUrl();
        if (!urlPrefix.endsWith("/")) {
            urlPrefix += "/";
        }
        String downloadUrl = urlPrefix + "releases/" + stored;
        return Result.success(Map.of("downloadUrl", downloadUrl, "storedPath", stored));
    }
}