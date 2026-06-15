package com.linkx.server.module.chat.dto;

import com.linkx.server.module.chat.constant.ChatConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SendMessageRequest {
    @NotNull(message = "用户ID不能为空")
    private Long toUserId;

    @NotBlank(message = "消息内容不能为空")
    private String content;

    private Integer msgType = ChatConstants.MESSAGE_TYPE_TEXT;

    private Integer sessionType = ChatConstants.SESSION_TYPE_SINGLE;
}
