package com.linkx.server.module.chat.dto;  // 行注：声明当前文件所在包 com.linkx.server.module.chat.dto

import lombok.Data;  // 行注：引入 Data 类型

import java.time.LocalDateTime;  // 行注：引入 LocalDateTime 类型

/** 会话列表项：对端用户或群、最后消息预览、未读数、置顶等。 */
@Data  // 行注：应用 @Data 注解
// 行注：定义 ChatSessionDTO 类
public class ChatSessionDTO {
    /** 会话记录主键 */
    private Long id;  // 行注：声明ID字段
    /** 会话所属当前用户 */
    private Long userId;  // 行注：声明用户ID字段
    /** 对端用户 ID 或群 ID */
    private Long targetId;  // 行注：声明targetID字段
    /** 单聊/群聊 */
    private Integer sessionType;  // 行注：声明会话类型字段
    /** 列表展示名（好友昵称或群名/群备注） */
    private String targetNickname;  // 行注：声明targetNickname字段
    /** 对端用户名或占位 group-{id} */
    private String targetUsername;  // 行注：声明targetUsername字段
    /** 头像 URL */
    private String targetAvatar;  // 行注：声明target头像字段
    /** 最后一条消息预览 */
    private String lastMessage;  // 行注：声明最后消息字段
    /** 最后消息时间 */
    private LocalDateTime lastMessageTime;  // 行注：声明最后消息时间字段
    /** 未读条数 */
    private Integer unreadCount;  // 行注：声明未读数量字段
    /** 群成员数（仅群聊） */
    private Integer memberCount;  // 行注：声明成员数量字段
    /** 我在群内的角色（仅群聊） */
    private Integer myRole;  // 行注：声明我的角色字段
    /** 我在本群的备注名 */
    private String groupRemark;  // 行注：声明群Remark字段
    /** 群公告全文 */
    private String notice;  // 行注：声明notice字段
    /** 是否有未读公告 */
    private Boolean noticeUnread;  // 行注：声明notice未读字段
    /** 是否被禁言 */
    private Boolean muted;  // 行注：声明muted字段
    /** 禁言截止时间 */
    private LocalDateTime muteTime;  // 行注：声明mute时间字段
    /** 是否开启消息免打扰 */
    private Boolean notificationMuted;  // 行注：声明通知Muted字段
    /** 单聊对端是否在线（WebSocket） */
    private Boolean targetOnline;  // 行注：声明target在线字段
    /** 会话备注（单聊/群聊会话级） */
    private String sessionRemark;
    /** 是否置顶 */
    private Boolean pinned;
}  // 行注：结束当前代码块
