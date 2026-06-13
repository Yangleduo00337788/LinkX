package com.linkx.server.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_friend_request")
public class SysFriendRequest {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long fromUserId;

    private Long toUserId;

    private String message;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
