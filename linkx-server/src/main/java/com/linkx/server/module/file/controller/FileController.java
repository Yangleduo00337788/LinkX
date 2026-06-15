package com.linkx.server.module.file.controller;

import com.linkx.server.common.Result;
import com.linkx.server.entity.SysFile;
import com.linkx.server.module.file.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping("/upload/avatar")
    public Result<SysFile> uploadAvatar(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("file") MultipartFile file) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return Result.success(fileService.uploadAvatar(userId, file));
    }

    @PostMapping("/upload/image")
    public Result<SysFile> uploadImage(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("file") MultipartFile file) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return Result.success(fileService.uploadImage(userId, file));
    }

    @GetMapping("/list")
    public Result<List<SysFile>> listFiles(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(value = "keyword", required = false) String keyword) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return Result.success(fileService.listFiles(userId, keyword));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteFile(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("id") Long id) {
        Long userId = Long.parseLong(userDetails.getUsername());
        fileService.deleteFile(userId, id);
        return Result.success();
    }
}
