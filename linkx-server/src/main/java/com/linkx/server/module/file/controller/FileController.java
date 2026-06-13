package com.linkx.server.module.file.controller;

import com.linkx.server.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileController {

    @Value("${linkx.upload.path:uploads/}")
    private String uploadPath;

    @Value("${linkx.upload.url:http://localhost:8080/uploads/}")
    private String uploadUrl;

    @PostMapping("/upload/avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return Result.error(400, "文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        String ext = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String filename = UUID.randomUUID().toString() + ext;

        String avatarDir = uploadPath + "avatar/";
        File dir = new File(avatarDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File dest = new File(avatarDir + filename);
        file.transferTo(dest);

        String url = uploadUrl + "avatar/" + filename;
        return Result.success(url);
    }

    @PostMapping("/upload/image")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return Result.error(400, "文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        String ext = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String filename = UUID.randomUUID().toString() + ext;

        String imageDir = uploadPath + "image/";
        File dir = new File(imageDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File dest = new File(imageDir + filename);
        file.transferTo(dest);

        String url = uploadUrl + "image/" + filename;
        return Result.success(url);
    }
}
