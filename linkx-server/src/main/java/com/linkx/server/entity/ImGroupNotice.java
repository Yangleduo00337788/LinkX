package com.linkx.server.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("im_group_notice")
public class ImGroupNotice {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long groupId;
    private String content;
    private Integer pinned;
    private Long publisherId;
  private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}