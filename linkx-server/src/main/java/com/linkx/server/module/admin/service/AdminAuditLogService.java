package com.linkx.server.module.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linkx.server.entity.SysAuditLog;
import com.linkx.server.mapper.SysAuditLogMapper;
import com.linkx.server.module.admin.dto.AdminAuditLogListItemDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AdminAuditLogService {

    private final SysAuditLogMapper auditLogMapper;

    public Page<AdminAuditLogListItemDTO> list(int page, int size, String action) {
        LambdaQueryWrapper<SysAuditLog> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(action)) {
            wrapper.eq(SysAuditLog::getAction, action.trim());
        }
        wrapper.orderByDesc(SysAuditLog::getCreateTime);
        Page<SysAuditLog> raw = auditLogMapper.selectPage(new Page<>(page, size), wrapper);
        Page<AdminAuditLogListItemDTO> result = new Page<>(raw.getCurrent(), raw.getSize(), raw.getTotal());
        result.setRecords(raw.getRecords().stream().map(l -> AdminAuditLogListItemDTO.builder()
                .id(l.getId())
                .adminUsername(l.getAdminUsername())
                .action(l.getAction())
                .targetType(l.getTargetType())
                .targetId(l.getTargetId())
                .detail(l.getDetail())
                .clientIp(l.getClientIp())
                .createTime(l.getCreateTime())
                .build()).toList());
        return result;
    }
}