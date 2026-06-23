package com.linkx.server.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/** 用户拉黑记录：拉黑后不可发消息/加好友等。 */
@Data
@TableName("sys_blacklist")
public class SysBlacklist {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;

    private Long blacklistUserId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
