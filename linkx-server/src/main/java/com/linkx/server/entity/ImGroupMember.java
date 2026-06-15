package com.linkx.server.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("im_group_member")
public class ImGroupMember {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long groupId;

    private Long userId;

    private Integer role;

    private LocalDateTime muteTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
