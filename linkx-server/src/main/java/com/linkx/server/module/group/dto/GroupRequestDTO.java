package com.linkx.server.module.group.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GroupRequestDTO {
    private Long id;
    private Long groupId;
    private String groupName;
    private String groupAvatar;
    private Long fromUserId;
    private String fromUsername;
    private String fromNickname;
    private String fromAvatar;
    private Long toUserId;
    private Integer requestType;
    private String message;
    private Integer status;
    private LocalDateTime createTime;
}
