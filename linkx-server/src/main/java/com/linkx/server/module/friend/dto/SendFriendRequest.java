package com.linkx.server.module.friend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 发送好友申请。 */
@Data
public class SendFriendRequest {
    @NotNull(message = "用户ID不能为空")
    private Long toUserId;

    private String message;
}
