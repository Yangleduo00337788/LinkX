package com.linkx.server.module.file.service;

import com.linkx.server.entity.SysFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileService {

    SysFile uploadImage(Long userId, MultipartFile file);

    SysFile uploadAvatar(Long userId, MultipartFile file);

    SysFile uploadFile(Long userId, MultipartFile file);

    List<SysFile> listFiles(Long userId, String keyword);

    void deleteFile(Long userId, Long fileId);
}
