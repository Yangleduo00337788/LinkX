package com.linkx.server.module.group.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class GroupDetailDTO {
    private Long id;
    private String groupName;
    private String groupAvatar;
    private String groupRemark;
    private String notice;
    private LocalDateTime noticeUpdateTime;
    private LocalDateTime noticeReadTime;
    private Boolean noticeUnread;
    private Long ownerId;
    private Integer maxMembers;
    private Integer memberCount;
    private Integer myRole;
    private Boolean muted;
    private LocalDateTime muteTime;
    private Boolean notificationMuted;
    private LocalDateTime createTime;
    private List<GroupMemberDTO> members;
}
