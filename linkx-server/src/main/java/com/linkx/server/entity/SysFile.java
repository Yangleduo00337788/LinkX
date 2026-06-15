package com.linkx.server.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_file")
public class SysFile {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;

    private String originalName;

    private String storedName;

    private String filePath;

    private String fileUrl;

    private Long fileSize;

    private String fileType;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
