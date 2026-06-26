package com.linkx.server.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 后台管理员 {@code sys_admin}，与 IM 用户账号分离。
 */
@Data
@TableName("sys_admin")
public class SysAdmin {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String username;

    @TableField("password_hash")
    private String password;

    @TableField("display_name")
    private String displayName;

    private String role;

    private Integer status;

    @TableField("last_login_time")
    private LocalDateTime lastLoginTime;

    @TableField("last_login_ip")
    private String lastLoginIp;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}