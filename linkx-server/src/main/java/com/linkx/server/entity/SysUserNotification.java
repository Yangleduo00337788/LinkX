package com.linkx.server.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_user_notification")
public class SysUserNotification {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long userId;
    private String title;
    private String content;
    private String bizType;
    private String bizId;
    private Integer readFlag;
    private LocalDateTime createTime;
}