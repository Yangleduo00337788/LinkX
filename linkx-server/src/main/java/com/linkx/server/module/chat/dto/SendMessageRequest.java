package com.linkx.server.module.chat.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SendMessageRequest {
    @NotNull(message = "用户ID不能为空")
    private Long toUserId;

    @NotNull(message = "消息内容不能为空")
    private String content;

    private Integer msgType = 0;
}
