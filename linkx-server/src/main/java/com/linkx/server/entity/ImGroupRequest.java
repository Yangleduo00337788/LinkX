package com.linkx.server.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("im_group_request")
public class ImGroupRequest {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long groupId;

    private Long fromUserId;

    private Long toUserId;

    private Integer requestType;

    private String message;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private LocalDateTime handleTime;
}
