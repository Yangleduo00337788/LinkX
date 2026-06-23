package com.linkx.server.module.group.dto;

import lombok.Data;

import java.time.LocalDateTime;

/** 群成员展示：昵称、角色、禁言截止等。 */
@Data
public class GroupMemberDTO {
    private Long id;
    private Long groupId;
    private Long userId;
    private String username;
    private String nickname;
    private String avatar;
    private Integer role;
    private Boolean muted;
    private LocalDateTime muteTime;
    private LocalDateTime createTime;
}
