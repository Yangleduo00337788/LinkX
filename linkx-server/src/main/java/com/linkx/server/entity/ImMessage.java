package com.linkx.server.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 聊天消息表 {@code im_message}：单聊/群聊内容、类型、@、已读、撤回、客户端幂等 ID。
 */
@Data
@TableName("im_message")
public class ImMessage {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long sessionId;

    private Long fromUserId;

    private Long toUserId;

    private String content;

    private Integer msgType;

    private Boolean mentionAll;

    private String mentionUserIds;

    private Integer status;

    private LocalDateTime readTime;

    private String clientMessageId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
