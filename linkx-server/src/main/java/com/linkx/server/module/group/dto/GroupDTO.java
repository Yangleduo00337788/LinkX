package com.linkx.server.module.group.dto;  // 行注：声明当前文件所在包 com.linkx.server.module.group.dto

import lombok.Data;  // 行注：引入 Data 类型

import java.time.LocalDateTime;  // 行注：引入 LocalDateTime 类型

/** 群列表摘要（我的群）。 */
@Data  // 行注：应用 @Data 注解
// 行注：定义 GroupDTO 类
public class GroupDTO {
    /** 主键 ID */
    private Long id;  // 行注：声明ID字段
    /** 群名称 */
    private String groupName;  // 行注：声明群名称字段
    /** 群头像 URL */
    private String groupAvatar;  // 行注：声明群头像字段
    /** 群公告 */
    private String notice;  // 行注：声明notice字段
    /** 群主用户 ID */
    private Long ownerId;  // 行注：声明所有者ID字段
    /** 最大成员数 */
    private Integer maxMembers;  // 行注：声明最大Members字段
    /** 成员数量 */
    private Integer memberCount;  // 行注：声明成员数量字段
    /** 当前用户在群内的角色 */
    private Integer myRole;  // 行注：声明我的角色字段
    /** 当前用户是否免打扰 */
    private Boolean muted;  // 行注：声明muted字段
    /** 禁言截止时间 */
    private LocalDateTime muteTime;  // 行注：声明mute时间字段
    /** 创建时间 */
    private LocalDateTime createTime;  // 行注：声明创建时间字段
}  // 行注：结束当前代码块
