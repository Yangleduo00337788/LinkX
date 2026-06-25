package com.linkx.server.module.friend.dto;  // 行注：声明当前文件所在包 com.linkx.server.module.friend.dto

import lombok.Data;  // 行注：引入 Data 类型

import java.time.LocalDateTime;  // 行注：引入 LocalDateTime 类型

/** 待处理的好友申请。 */
@Data  // 行注：应用 @Data 注解
// 行注：定义 FriendRequestDTO 类
public class FriendRequestDTO {
    /** 主键 ID */
    private Long id;  // 行注：声明ID字段
    /** 发起方用户 ID */
    private Long fromUserId;  // 行注：声明用户ID字段
    /** 发起方用户名 */
    private String fromUsername;  // 行注：声明Username字段
    /** 发起方昵称 */
    private String fromNickname;  // 行注：声明Nickname字段
    /** 发起方头像 URL */
    private String fromAvatar;  // 行注：声明头像字段
    /** 申请或消息附言 */
    private String message;  // 行注：声明消息字段
    /** 状态 */
    private Integer status;  // 行注：声明状态字段
    /** 创建时间 */
    private LocalDateTime createTime;  // 行注：声明创建时间字段
}  // 行注：结束当前代码块
