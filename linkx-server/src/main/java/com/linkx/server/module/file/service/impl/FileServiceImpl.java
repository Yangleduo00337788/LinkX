package com.linkx.server.module.file.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.linkx.server.common.BusinessException;
import com.linkx.server.common.ErrorCode;
import com.linkx.server.entity.SysFile;
import com.linkx.server.mapper.SysFileMapper;
import com.linkx.server.module.file.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final SysFileMapper fileMapper;

    @Value("${linkx.upload.path:uploads/}")
    private String uploadPath;

    @Value("${linkx.upload.url:http://localhost:8080/uploads/}")
    private String uploadUrl;

    @Override
    public SysFile uploadImage(Long userId, MultipartFile file) {
        return uploadFile(userId, file, "image");
    }

    @Override
    public SysFile uploadAvatar(Long userId, MultipartFile file) {
        return uploadFile(userId, file, "avatar");
    }

    private SysFile uploadFile(Long userId, MultipartFile file, String type) {
        if (file.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        String ext = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String storedName = UUID.randomUUID().toString() + ext;
        String dirPath = uploadPath + type + "/";
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try {
            File dest = new File(dirPath + storedName);
            file.transferTo(dest);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "文件上传失败");
        }

        String fileUrl = uploadUrl + type + "/" + storedName;

        SysFile sysFile = new SysFile();
        sysFile.setUserId(userId);
        sysFile.setOriginalName(originalFilename);
        sysFile.setStoredName(storedName);
        sysFile.setFilePath(dirPath + storedName);
        sysFile.setFileUrl(fileUrl);
        sysFile.setFileSize(file.getSize());
        sysFile.setFileType(file.getContentType());
        fileMapper.insert(sysFile);

        return sysFile;
    }

    @Override
    public List<SysFile> listFiles(Long userId, String keyword) {
        LambdaQueryWrapper<SysFile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysFile::getUserId, userId);
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(SysFile::getOriginalName, keyword);
        }
        wrapper.orderByDesc(SysFile::getCreateTime);
        return fileMapper.selectList(wrapper);
    }

    @Override
    public void deleteFile(Long userId, Long fileId) {
        SysFile sysFile = fileMapper.selectById(fileId);
        if (sysFile == null || !sysFile.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }

        File file = new File(sysFile.getFilePath());
        if (file.exists()) {
            file.delete();
        }

        fileMapper.deleteById(fileId);
    }
}
