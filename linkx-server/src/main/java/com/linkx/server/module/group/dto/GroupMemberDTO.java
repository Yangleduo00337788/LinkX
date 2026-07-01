package com.linkx.server.module.group.dto;  // 行注：声明当前文件所在包 com.linkx.server.module.group.dto

import lombok.Data;  // 行注：引入 Data 类型

import java.time.LocalDateTime;  // 行注：引入 LocalDateTime 类型

/** 群成员展示：昵称、角色、禁言截止等。 */
@Data  // 行注：应用 @Data 注解
// 行注：定义 GroupMemberDTO 类
public class GroupMemberDTO {
    /** 主键 ID */
    private Long id;  // 行注：声明ID字段
    /** 群 ID */
    private Long groupId;  // 行注：声明群ID字段
    /** 用户 ID */
    private Long userId;  // 行注：声明用户ID字段
    /** 用户名 */
    private String username;  // 行注：声明username字段
    /** 昵称 */
    private String nickname;  // 行注：声明nickname字段
    /** 我在本群的群名片 */
    private String memberCardName;
    /** 头像 URL */
    private String avatar;  // 行注：声明头像字段
    /** 角色 */
    private Integer role;  // 行注：声明角色字段
    /** 当前用户是否免打扰 */
    private Boolean muted;  // 行注：声明muted字段
    /** 禁言截止时间 */
    private LocalDateTime muteTime;  // 行注：声明mute时间字段
    /** 创建时间 */
    private LocalDateTime createTime;  // 行注：声明创建时间字段
}  // 行注：结束当前代码块
