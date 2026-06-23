package com.linkx.server.module.group.dto;

import lombok.Data;

import java.time.LocalDateTime;

/** 群列表摘要（我的群）。 */
@Data
public class GroupDTO {
    private Long id;
    private String groupName;
    private String groupAvatar;
    private String notice;
    private Long ownerId;
    private Integer maxMembers;
    private Integer memberCount;
    private Integer myRole;
    private Boolean muted;
    private LocalDateTime muteTime;
    private LocalDateTime createTime;
}
