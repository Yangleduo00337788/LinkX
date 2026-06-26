package com.linkx.server.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_app_release")
public class SysAppRelease {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String platform;

    private String version;

    private String downloadUrl;

    private String releaseNotes;

    private Integer forceUpdate;

    private Integer published;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}