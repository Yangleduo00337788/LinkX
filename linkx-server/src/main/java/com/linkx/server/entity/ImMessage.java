package com.linkx.server.entity;  // 行注：声明当前文件所在包 com.linkx.server.entity

import com.baomidou.mybatisplus.annotation.*;  // 行注：引入 * 类型
import lombok.Data;  // 行注：引入 Data 类型

import java.time.LocalDateTime;  // 行注：引入 LocalDateTime 类型

/**
 * 聊天消息表 {@code im_message}：单聊/群聊内容、类型、@、已读、撤回、客户端幂等 ID。
 */
@Data  // 行注：应用 @Data 注解
@TableName("im_message")  // 行注：应用 @TableName 注解
// 行注：定义 ImMessage 类
public class ImMessage {

    /** 主键 ID */
    @TableId(type = IdType.ASSIGN_ID)  // 行注：应用 @TableId 注解
    private Long id;  // 行注：声明ID字段

    /** 会话 ID */
    private Long sessionId;  // 行注：声明会话ID字段

    /** 发起方用户 ID */
    private Long fromUserId;  // 行注：声明用户ID字段

    /** 目标用户 ID */
    private Long toUserId;  // 行注：声明转为用户ID字段

    /** 消息内容 */
    private String content;  // 行注：声明内容字段

    /** 消息类型 */
    private Integer msgType;  // 行注：声明消息类型字段

    /** 是否 @ 全员 */
    private Boolean mentionAll;  // 行注：声明@提醒全部字段

    /** 被 @ 用户 ID 列表 */
    private String mentionUserIds;  // 行注：声明@提醒用户ID列表字段

    /** 状态 */
    private Integer status;  // 行注：声明状态字段

    /** 已读时间 */
    private LocalDateTime readTime;  // 行注：声明已读时间字段

    /** 客户端幂等消息 ID */
    private String clientMessageId;  // 行注：声明客户端消息ID字段

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)  // 行注：应用 @TableField 注解
    private LocalDateTime createTime;  // 行注：声明创建时间字段
}  // 行注：结束当前代码块
