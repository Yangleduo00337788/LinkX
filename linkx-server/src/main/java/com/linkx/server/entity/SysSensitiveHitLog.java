package com.linkx.server.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_sensitive_hit_log")
public class SysSensitiveHitLog {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long userId;
    private Long wordId;
    private String matchedWord;
    private String contentSnippet;
    private String source;
    private Integer blocked;
    private Long messageId;
    private LocalDateTime createTime;
}