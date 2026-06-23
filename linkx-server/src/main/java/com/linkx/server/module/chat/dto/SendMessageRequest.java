package com.linkx.server.module.chat.dto;

import com.linkx.server.module.chat.constant.ChatConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/** REST 发送文本/系统消息；群聊时 toUserId 为 groupId。 */
@Data
public class SendMessageRequest {
    @NotNull(message = "用户ID不能为空")
    private Long toUserId;

    @NotBlank(message = "消息内容不能为空")
    private String content;

    private Integer msgType = ChatConstants.MESSAGE_TYPE_TEXT;

    private Integer sessionType = ChatConstants.SESSION_TYPE_SINGLE;

    private Boolean mentionAll = false;

    private List<Long> mentionUserIds;

    @Size(max = 64, message = "clientMessageId 长度不能超过64")
    private String clientMessageId;
}
