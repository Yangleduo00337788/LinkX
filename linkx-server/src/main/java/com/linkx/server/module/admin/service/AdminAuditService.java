package com.linkx.server.module.admin.service;

import com.linkx.server.entity.SysAuditLog;
import com.linkx.server.mapper.SysAuditLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminAuditService {

    private final SysAuditLogMapper auditLogMapper;

    public void record(Long adminId, String adminUsername, String action,
                       String targetType, String targetId, String detail, String clientIp) {
        SysAuditLog log = new SysAuditLog();
        log.setAdminId(adminId);
        log.setAdminUsername(adminUsername);
        log.setAction(action);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setDetail(detail);
        log.setClientIp(clientIp);
        auditLogMapper.insert(log);
    }
}