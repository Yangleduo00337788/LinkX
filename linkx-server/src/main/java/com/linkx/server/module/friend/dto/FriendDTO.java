package com.linkx.server.module.friend.dto;  // 行注：声明当前文件所在包 com.linkx.server.module.friend.dto

import lombok.Data;  // 行注：引入 Data 类型

import java.time.LocalDateTime;  // 行注：引入 LocalDateTime 类型

/** 好友列表项。 */
@Data  // 行注：应用 @Data 注解
// 行注：定义 FriendDTO 类
public class FriendDTO {
    /** 主键 ID */
    private Long id;  // 行注：声明ID字段
    /** 用户 ID */
    private Long userId;  // 行注：声明用户ID字段
    /** 好友用户 ID */
    private Long friendId;  // 行注：声明好友ID字段
    /** 好友用户名 */
    private String friendUsername;  // 行注：声明好友Username字段
    /** 好友昵称 */
    private String friendNickname;  // 行注：声明好友Nickname字段
    /** 好友头像 URL */
    private String friendAvatar;  // 行注：声明好友头像字段
    /** 备注 */
    private String remark;  // 行注：声明remark字段
    /** 创建时间 */
    private LocalDateTime createTime;  // 行注：声明创建时间字段
}  // 行注：结束当前代码块
