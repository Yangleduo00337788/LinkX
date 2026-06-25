package com.linkx.server.module.file.service;  // 行注：声明当前文件所在包 com.linkx.server.module.file.service

import com.linkx.server.entity.SysFile;
import com.linkx.server.module.file.dto.FileAccessDTO;
import com.linkx.server.module.file.dto.FileDTO;
import com.linkx.server.module.file.dto.StoredFileContent;
import org.springframework.web.multipart.MultipartFile;  // 行注：引入 MultipartFile 类型

import java.util.List;  // 行注：引入 List 类型

/**
 * 文件上传、列表、删除及为已登录用户签发短期访问 ticket。
 */
// 行注：定义 FileService 接口
public interface FileService {

    /**
     * 上传图片。
     *
     * @param userId 用户 ID
     * @param file 上传文件
     * @return 文件信息
     */
    FileDTO uploadImage(Long userId, MultipartFile file);  // 行注：调用上传Image

    /**
     * 上传头像。
     *
     * @param userId 用户 ID
     * @param file 上传文件
     * @return 文件信息
     */
    FileDTO uploadAvatar(Long userId, MultipartFile file);  // 行注：调用上传头像

    /**
     * 上传文件。
     *
     * @param userId 用户 ID
     * @param file 上传文件
     * @return 文件信息
     */
    FileDTO uploadFile(Long userId, MultipartFile file);  // 行注：调用上传文件

    /**
     * 查询文件列表。
     *
     * @param userId 用户 ID
     * @param keyword 搜索关键词
     * @return 文件信息列表
     */
    List<FileDTO> listFiles(Long userId, String keyword);  // 行注：调用列表Files

    /** 将库内 fileUrl 转为带 ticket 的 access URL */
    FileAccessDTO createAccessUrl(Long userId, String fileUrl);  // 行注：调用创建访问URL

    /**
     * 删除文件。
     *
     * @param userId 用户 ID
     * @param fileId 文件 ID
     */
    void deleteFile(Long userId, Long fileId);

    /** 按 ticket 校验后打开文件内容（MinIO 或本地磁盘） */
    StoredFileContent openStoredContent(SysFile sysFile);
}
