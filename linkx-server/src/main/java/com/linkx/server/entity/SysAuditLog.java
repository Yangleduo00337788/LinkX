package com.linkx.server.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_audit_log")
public class SysAuditLog {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long adminId;

    private String adminUsername;

    private String action;

    private String targetType;

    private String targetId;

    private String detail;

    private String clientIp;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}