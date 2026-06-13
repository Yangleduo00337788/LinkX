package com.linkx.server.module.friend.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FriendRequestDTO {
    private Long id;
    private Long fromUserId;
    private String fromUsername;
    private String fromNickname;
    private String fromAvatar;
    private String message;
    private Integer status;
    private LocalDateTime createTime;
}
