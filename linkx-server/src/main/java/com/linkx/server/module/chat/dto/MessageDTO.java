package com.linkx.server.module.chat.dto;  // 行注：声明当前文件所在包 com.linkx.server.module.chat.dto

import lombok.Data;  // 行注：引入 Data 类型

import java.time.LocalDateTime;  // 行注：引入 LocalDateTime 类型
import java.util.List;  // 行注：引入 List 类型

/** 聊天消息 API/WS 传输对象。 */
@Data  // 行注：应用 @Data 注解
// 行注：定义 MessageDTO 类
public class MessageDTO {
    /** 消息主键 */
    private Long id;  // 行注：声明ID字段
    /** 所属会话 ID（群聊时可能与 groupId 相同） */
    private Long sessionId;  // 行注：声明会话ID字段
    /** 客户端幂等 ID */
    private String clientMessageId;  // 行注：声明客户端消息ID字段
    /** 发送者用户 ID */
    private Long fromUserId;  // 行注：声明用户ID字段
    /** 发送者昵称（展示用） */
    private String fromNickname;  // 行注：声明Nickname字段
    /** 发送者头像 URL */
    private String fromAvatar;  // 行注：声明头像字段
    /** 单聊对端用户 ID 或群 ID */
    private Long toUserId;  // 行注：声明转为用户ID字段
    /** 会话类型：单聊/群聊 */
    private Integer sessionType;  // 行注：声明会话类型字段
    /** 文本内容或文件 URL */
    private String content;  // 行注：声明内容字段
    /** 消息类型，见 ChatConstants */
    private Integer msgType;  // 行注：声明消息类型字段
    /** 是否 @全员（群聊） */
    private Boolean mentionAll;  // 行注：声明@提醒全部字段
    /** 被 @ 的用户 ID 列表 */
    private List<Long> mentionUserIds;  // 行注：声明@提醒用户ID列表字段
    /** 被 @ 用户的展示名列表 */
    private List<String> mentionDisplayNames;  // 行注：声明@提醒DisplayNames字段
    /** 附件原始文件名 */
    private String fileName;  // 行注：声明文件名称字段
    /** 附件大小（字节） */
    private Long fileSize;  // 行注：声明文件大小字段
    /** 附件 MIME 或扩展类型 */
    private String fileType;  // 行注：声明文件类型字段
    /** 消息状态：正常/已撤回 */
    private Integer status;  // 行注：声明状态字段
    /** 单聊已读时间（群聊可能为空） */
    private LocalDateTime readTime;  // 行注：声明已读时间字段
    /** 发送时间 */
    private LocalDateTime createTime;  // 行注：声明创建时间字段
}  // 行注：结束当前代码块
