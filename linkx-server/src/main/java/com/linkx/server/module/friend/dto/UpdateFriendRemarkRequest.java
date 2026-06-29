package com.linkx.server.module.friend.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateFriendRemarkRequest {
    @Size(max = 64, message = "好友备注长度不能超过64个字符")
    private String remark;
}