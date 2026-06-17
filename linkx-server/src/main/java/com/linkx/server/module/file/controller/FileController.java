package com.linkx.server.module.file.controller;

import com.linkx.server.common.Result;
import com.linkx.server.module.file.dto.FileDTO;
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
    public Result<FileDTO> uploadAvatar(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("file") MultipartFile file) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return Result.success(fileService.uploadAvatar(userId, file));
    }

    @PostMapping("/upload/image")
    public Result<FileDTO> uploadImage(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("file") MultipartFile file) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return Result.success(fileService.uploadImage(userId, file));
    }

    @PostMapping("/upload/file")
    public Result<FileDTO> uploadFile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("file") MultipartFile file) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return Result.success(fileService.uploadFile(userId, file));
    }

    @GetMapping("/list")
    public Result<List<FileDTO>> listFiles(
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
