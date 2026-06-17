package com.linkx.server.module.file.service;

import com.linkx.server.module.file.dto.FileDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileService {

    FileDTO uploadImage(Long userId, MultipartFile file);

    FileDTO uploadAvatar(Long userId, MultipartFile file);

    FileDTO uploadFile(Long userId, MultipartFile file);

    List<FileDTO> listFiles(Long userId, String keyword);

    void deleteFile(Long userId, Long fileId);
}
