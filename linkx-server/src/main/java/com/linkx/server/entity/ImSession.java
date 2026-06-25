package com.linkx.server.entity;  // 行注：声明当前文件所在包 com.linkx.server.entity

import com.baomidou.mybatisplus.annotation.*;  // 行注：引入 * 类型
import lombok.Data;  // 行注：引入 Data 类型

import java.time.LocalDateTime;  // 行注：引入 LocalDateTime 类型

/** 用户会话列表项：单聊对端或群 ID、置顶、未读、最后一条预览等。 */
@Data  // 行注：应用 @Data 注解
@TableName("im_session")  // 行注：应用 @TableName 注解
// 行注：定义 ImSession 类
public class ImSession {

    /** 主键 ID */
    @TableId(type = IdType.ASSIGN_ID)  // 行注：应用 @TableId 注解
    private Long id;  // 行注：声明ID字段

    /** 用户 ID */
    private Long userId;  // 行注：声明用户ID字段

    /** 对端对象 ID */
    private Long targetId;  // 行注：声明targetID字段

    /** 会话类型 */
    private Integer sessionType;  // 行注：声明会话类型字段

    /** 最后一条消息摘要 */
    private String lastMessage;  // 行注：声明最后消息字段

    /** 最后一条消息时间 */
    private LocalDateTime lastMessageTime;  // 行注：声明最后消息时间字段

    /** 未读数量 */
    private Integer unreadCount;  // 行注：声明未读数量字段

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)  // 行注：应用 @TableField 注解
    private LocalDateTime createTime;  // 行注：声明创建时间字段
}  // 行注：结束当前代码块
