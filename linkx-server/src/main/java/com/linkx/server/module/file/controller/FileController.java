package com.linkx.server.module.file.controller;

import com.linkx.server.common.Result;
import com.linkx.server.entity.SysFile;
import com.linkx.server.module.file.dto.FileAccessDTO;
import com.linkx.server.module.file.dto.FileDTO;
import com.linkx.server.module.file.service.FileAccessTicketService;
import com.linkx.server.module.file.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;
    private final FileAccessTicketService fileAccessTicketService;

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

    @GetMapping("/access-url")
    public Result<FileAccessDTO> createAccessUrl(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("fileUrl") String fileUrl) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return Result.success(fileService.createAccessUrl(userId, fileUrl));
    }

    @GetMapping("/access/{ticket}")
    public ResponseEntity<Resource> accessFile(@PathVariable("ticket") String ticket) throws MalformedURLException {
        SysFile sysFile = fileAccessTicketService.resolveFile(ticket);
        if (sysFile == null) {
            return ResponseEntity.notFound().build();
        }
        Path filePath = Path.of(sysFile.getFilePath());
        Resource resource = new UrlResource(filePath.toUri());
        if (!resource.exists() || !resource.isReadable()) {
            return ResponseEntity.notFound().build();
        }
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (sysFile.getFileType() != null && !sysFile.getFileType().isBlank()) {
            mediaType = MediaType.parseMediaType(sysFile.getFileType());
        }
        String disposition = mediaType.getType().equalsIgnoreCase("image") ? "inline" : "attachment";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition + "; filename=\"" + resource.getFilename() + "\"")
                .contentType(mediaType)
                .body(resource);
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
