package com.linkx.server.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_friend")
public class SysFriend {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;

    private Long friendId;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
