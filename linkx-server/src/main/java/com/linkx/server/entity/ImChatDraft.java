package com.linkx.server.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("im_chat_draft")
public class ImChatDraft {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;

    private Long targetId;

    private Integer sessionType;

    private String draftContent;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}