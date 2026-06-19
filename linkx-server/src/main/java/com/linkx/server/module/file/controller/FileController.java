package com.linkx.server.module.file.controller;

import com.linkx.server.common.Result;
import com.linkx.server.entity.SysFile;
import com.linkx.server.module.file.dto.FileAccessDTO;
import com.linkx.server.module.file.dto.FileDTO;
import com.linkx.server.module.file.service.FileAccessTicketService;
import com.linkx.server.module.file.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
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
        FileDTO response = fileService.uploadAvatar(userId, file);
        log.info("File avatar upload success, userId={}, fileId={}, originalName={}", userId, response.getId(), response.getOriginalName());
        return Result.success(response);
    }

    @PostMapping("/upload/image")
    public Result<FileDTO> uploadImage(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("file") MultipartFile file) {
        Long userId = Long.parseLong(userDetails.getUsername());
        FileDTO response = fileService.uploadImage(userId, file);
        log.info("File image upload success, userId={}, fileId={}, originalName={}", userId, response.getId(), response.getOriginalName());
        return Result.success(response);
    }

    @PostMapping("/upload/file")
    public Result<FileDTO> uploadFile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("file") MultipartFile file) {
        Long userId = Long.parseLong(userDetails.getUsername());
        FileDTO response = fileService.uploadFile(userId, file);
        log.info("File upload success, userId={}, fileId={}, originalName={}", userId, response.getId(), response.getOriginalName());
        return Result.success(response);
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
        FileAccessDTO response = fileService.createAccessUrl(userId, fileUrl);
        log.info("File access url created, userId={}, fileUrl={}", userId, fileUrl);
        return Result.success(response);
    }

    @GetMapping("/access/{ticket}")
    public ResponseEntity<Resource> accessFile(@PathVariable("ticket") String ticket) throws MalformedURLException {
        SysFile sysFile = fileAccessTicketService.resolveFile(ticket);
        if (sysFile == null) {
            log.warn("File access rejected, reason=invalid_ticket, ticket={}", ticket);
            return ResponseEntity.notFound().build();
        }
        Path filePath = Path.of(sysFile.getFilePath());
        Resource resource = new UrlResource(filePath.toUri());
        if (!resource.exists() || !resource.isReadable()) {
            log.warn("File access rejected, reason=file_unavailable, ticket={}, fileId={}, path={}",
                    ticket, sysFile.getId(), sysFile.getFilePath());
            return ResponseEntity.notFound().build();
        }
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (sysFile.getFileType() != null && !sysFile.getFileType().isBlank()) {
            mediaType = MediaType.parseMediaType(sysFile.getFileType());
        }
        String disposition = mediaType.getType().equalsIgnoreCase("image") ? "inline" : "attachment";
        log.info("File access granted, ticket={}, fileId={}, originalName={}", ticket, sysFile.getId(), sysFile.getOriginalName());
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
        log.info("File delete success, userId={}, fileId={}", userId, id);
        return Result.success();
    }
}
