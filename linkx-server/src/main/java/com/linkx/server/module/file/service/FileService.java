package com.linkx.server.module.file.service;

import com.linkx.server.module.file.dto.FileAccessDTO;
import com.linkx.server.module.file.dto.FileDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文件上传、列表、删除及为已登录用户签发短期访问 ticket。
 */
public interface FileService {

    FileDTO uploadImage(Long userId, MultipartFile file);

    FileDTO uploadAvatar(Long userId, MultipartFile file);

    FileDTO uploadFile(Long userId, MultipartFile file);

    List<FileDTO> listFiles(Long userId, String keyword);

    /** 将库内 fileUrl 转为带 ticket 的 access URL */
    FileAccessDTO createAccessUrl(Long userId, String fileUrl);

    void deleteFile(Long userId, Long fileId);
}