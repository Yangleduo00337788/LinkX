package com.linkx.server.common;  // 行注：声明当前文件所在包 com.linkx.server.common

import lombok.extern.slf4j.Slf4j;  // 行注：引入 Slf4j 类型
import org.springframework.stereotype.Service;  // 行注：引入 Service 类型

/**
 * 结构化审计日志（当前输出到应用日志，便于 ELK 等采集）。
 * <p>格式前缀 {@code AUDIT}，含 action、操作者、目标类型与 ID。</p>
 */
@Slf4j  // 行注：应用 @Slf4j 注解
@Service  // 行注：应用 @Service 注解
// 行注：定义 AuditLogService 类
public class AuditLogService {

    /**
     * 记录一次成功的敏感操作（如登录、改密、踢人等）。
     *
     * @param action     操作动作标识，如 {@code AUTH_LOGIN}
     * @param operatorId 操作者用户 ID，系统任务可为 null
     * @param targetType 目标类型，如 {@code USER}、{@code GROUP}
     * @param targetId   目标主键
     * @param detail     附加说明，会做空安全处理
     */
    /**
     * 记录操作成功审计。
     *
     * @param action     动作标识，如 login、send_message
     * @param operatorId 操作者用户 ID
     * @param targetType 目标类型，如 user、group
     * @param targetId   目标主键
     * @param detail     补充说明，可为 null
     */
    // 行注：定义记录Success方法
    public void recordSuccess(String action, Long operatorId, String targetType, Object targetId, String detail) {
        // 使用固定字段顺序，便于日志平台按 action/operatorId 检索
        // 行注：补充当前表达式片段
        log.info("AUDIT action={}, result=SUCCESS, operatorId={}, targetType={}, targetId={}, detail={}",
                action, operatorId, targetType, targetId, safeDetail(detail));  // 行注：调用safe详情
    }  // 行注：结束当前代码块

    /**
     * 记录一次失败或拒绝的敏感操作尝试。
     *
     * @param action     操作动作标识
     * @param operatorId 操作者用户 ID
     * @param targetType 目标类型
     * @param targetId   目标主键
     * @param detail     失败原因摘要
     */
    /** 记录操作失败或拒绝审计（日志级别为 warn） */
    // 行注：定义记录Failure方法
    public void recordFailure(String action, Long operatorId, String targetType, Object targetId, String detail) {
        // 行注：补充当前表达式片段
        log.warn("AUDIT action={}, result=FAILURE, operatorId={}, targetType={}, targetId={}, detail={}",
                action, operatorId, targetType, targetId, safeDetail(detail));  // 行注：调用safe详情
    }  // 行注：结束当前代码块

    /** 避免日志中出现字面量 "null" 字符串 */
    // 行注：定义safe详情方法
    private String safeDetail(String detail) {
        return detail == null ? "" : detail;  // 行注：返回处理结果
    }  // 行注：结束当前代码块
}  // 行注：结束当前代码块
