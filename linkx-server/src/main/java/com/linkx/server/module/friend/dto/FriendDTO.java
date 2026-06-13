package com.linkx.server.module.friend.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FriendDTO {
    private Long id;
    private Long userId;
    private Long friendId;
    private String friendUsername;
    private String friendNickname;
    private String friendAvatar;
    private String remark;
    private LocalDateTime createTime;
}
