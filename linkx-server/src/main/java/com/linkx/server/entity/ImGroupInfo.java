package com.linkx.server.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 群组资料：名称、头像、公告、群主、最大人数等。 */
@Data
@TableName("im_group_info")
public class ImGroupInfo {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String groupName;

    private String groupAvatar;

    private Long ownerId;

    private Integer maxMembers;

    private String notice;

    private LocalDateTime noticeUpdateTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    private Integer deleted;
}
