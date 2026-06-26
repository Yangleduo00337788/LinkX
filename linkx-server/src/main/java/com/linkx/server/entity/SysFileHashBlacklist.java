package com.linkx.server.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_file_hash_blacklist")
public class SysFileHashBlacklist {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String contentHash;
    private String reason;
    private Integer enabled;
    private LocalDateTime createTime;
}