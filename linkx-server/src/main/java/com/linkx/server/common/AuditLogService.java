package com.linkx.server.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 结构化审计日志（当前输出到应用日志，便于 ELK 等采集）。
 * <p>格式前缀 {@code AUDIT}，含 action、操作者、目标类型与 ID。</p>
 */
@Slf4j
@Service
public class AuditLogService {

    public void recordSuccess(String action, Long operatorId, String targetType, Object targetId, String detail) {
        log.info("AUDIT action={}, result=SUCCESS, operatorId={}, targetType={}, targetId={}, detail={}",
                action, operatorId, targetType, targetId, safeDetail(detail));
    }

    public void recordFailure(String action, Long operatorId, String targetType, Object targetId, String detail) {
        log.warn("AUDIT action={}, result=FAILURE, operatorId={}, targetType={}, targetId={}, detail={}",
                action, operatorId, targetType, targetId, safeDetail(detail));
    }

    private String safeDetail(String detail) {
        return detail == null ? "" : detail;
    }
}