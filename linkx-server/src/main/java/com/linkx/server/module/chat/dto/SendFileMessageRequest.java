package com.linkx.server.module.chat.dto;  // 行注：声明当前文件所在包 com.linkx.server.module.chat.dto

import com.linkx.server.module.chat.constant.ChatConstants;  // 行注：引入 ChatConstants 类型
import jakarta.validation.constraints.NotNull;  // 行注：引入 NotNull 类型
import lombok.Data;  // 行注：引入 Data 类型

/** REST 发送图片/文件类消息，content 存 fileId 或 JSON 元数据。 */
@Data  // 行注：应用 @Data 注解
// 行注：定义 SendFileMessageRequest 类
public class SendFileMessageRequest {
    /** 单聊对端或群 ID */
    @NotNull(message = "用户ID不能为空")  // 行注：应用 @NotNull 注解
    private Long toUserId;  // 行注：声明转为用户ID字段

    /** 已通过上传接口登记的文件主键 */
    @NotNull(message = "文件ID不能为空")  // 行注：应用 @NotNull 注解
    private Long fileId;  // 行注：声明文件ID字段

    /** 图片或文件类型，见 ChatConstants */
    @NotNull(message = "消息类型不能为空")  // 行注：应用 @NotNull 注解
    private Integer msgType;  // 行注：声明消息类型字段

    private Integer sessionType = ChatConstants.SESSION_TYPE_SINGLE;  // 行注：声明会话类型字段

    /** 客户端幂等 ID */
    private String clientMessageId;  // 行注：声明客户端消息ID字段
}  // 行注：结束当前代码块
