package com.linkx.server.module.chat.dto;

import com.linkx.server.module.chat.constant.ChatConstants;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SendFileMessageRequest {
    @NotNull(message = "用户ID不能为空")
    private Long toUserId;

    @NotNull(message = "文件ID不能为空")
    private Long fileId;

    @NotNull(message = "消息类型不能为空")
    private Integer msgType;

    private Integer sessionType = ChatConstants.SESSION_TYPE_SINGLE;

    private String clientMessageId;
}
