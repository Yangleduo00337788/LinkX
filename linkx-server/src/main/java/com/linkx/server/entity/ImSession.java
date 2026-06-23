package com.linkx.server.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/** 用户会话列表项：单聊对端或群 ID、置顶、未读、最后一条预览等。 */
@Data
@TableName("im_session")
public class ImSession {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;

    private Long targetId;

    private Integer sessionType;

    private String lastMessage;

    private LocalDateTime lastMessageTime;

    private Integer unreadCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
