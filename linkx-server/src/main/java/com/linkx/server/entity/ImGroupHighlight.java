package com.linkx.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("im_group_highlight")
public class ImGroupHighlight {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long groupId;
    private Long messageId;
    private String title;
    private Long createdBy;
    private LocalDateTime createTime;
}