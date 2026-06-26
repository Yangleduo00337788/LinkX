package com.linkx.server.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_sensitive_word")
public class SysSensitiveWord {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String word;
    private Integer matchMode;
    private String category;
    private Integer action;
    private Integer enabled;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}