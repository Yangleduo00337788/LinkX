package com.linkx.server.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_report")
public class SysReport {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long reporterUserId;
    private String targetType;
    private String targetId;
    private String reasonCategory;
    private String reasonDetail;
    private Integer status;
    private Long handlerAdminId;
    private String resolution;
    private String resolutionNote;
    private Integer notifyReporter;
    private Integer reporterNotified;
    private LocalDateTime handledTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}