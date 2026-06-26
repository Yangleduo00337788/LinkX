package com.linkx.server.module.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linkx.server.common.BusinessException;
import com.linkx.server.common.ErrorCode;
import com.linkx.server.entity.SysFile;
import com.linkx.server.mapper.SysFileMapper;
import com.linkx.server.module.admin.dto.AdminFileListItemDTO;
import com.linkx.server.module.file.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminFileService {

    private final SysFileMapper fileMapper;
    private final FileService fileService;

    public Page<AdminFileListItemDTO> list(int page, int size, Long userId) {
        LambdaQueryWrapper<SysFile> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(SysFile::getUserId, userId);
        }
        wrapper.orderByDesc(SysFile::getCreateTime);
        Page<SysFile> raw = fileMapper.selectPage(new Page<>(page, size), wrapper);
        Page<AdminFileListItemDTO> result = new Page<>(raw.getCurrent(), raw.getSize(), raw.getTotal());
        result.setRecords(raw.getRecords().stream().map(f -> AdminFileListItemDTO.builder()
                .id(f.getId())
                .userId(f.getUserId())
                .originalName(f.getOriginalName())
                .fileSize(f.getFileSize())
                .fileType(f.getFileType())
                .createTime(f.getCreateTime())
                .build()).toList());
        return result;
    }

    @Transactional
    public void delete(Long fileId, Long adminId, String adminUsername, String clientIp, AdminAuditService audit) {
        SysFile file = fileMapper.selectById(fileId);
        if (file == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        try {
            fileService.deleteFile(file.getUserId(), fileId);
        } catch (Exception ignored) {
            fileMapper.deleteById(fileId);
        }
        audit.record(adminId, adminUsername, "FILE_DELETE", "file", String.valueOf(fileId),
                file.getOriginalName(), clientIp);
    }
}