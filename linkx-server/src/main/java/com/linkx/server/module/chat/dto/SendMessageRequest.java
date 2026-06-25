package com.linkx.server.module.chat.dto;  // 行注：声明当前文件所在包 com.linkx.server.module.chat.dto

import com.linkx.server.module.chat.constant.ChatConstants;  // 行注：引入 ChatConstants 类型
import jakarta.validation.constraints.NotBlank;  // 行注：引入 NotBlank 类型
import jakarta.validation.constraints.NotNull;  // 行注：引入 NotNull 类型
import jakarta.validation.constraints.Size;  // 行注：引入 Size 类型
import lombok.Data;  // 行注：引入 Data 类型

import java.util.List;  // 行注：引入 List 类型

/** REST 发送文本/系统消息；群聊时 toUserId 为 groupId。 */
@Data  // 行注：应用 @Data 注解
// 行注：定义 SendMessageRequest 类
public class SendMessageRequest {
    /** 单聊：对方用户 ID；群聊：群 ID */
    @NotNull(message = "用户ID不能为空")  // 行注：应用 @NotNull 注解
    private Long toUserId;  // 行注：声明转为用户ID字段

    /** 消息正文 */
    @NotBlank(message = "消息内容不能为空")  // 行注：应用 @NotBlank 注解
    private String content;  // 行注：声明内容字段

    /** 默认文本消息 */
    private Integer msgType = ChatConstants.MESSAGE_TYPE_TEXT;  // 行注：声明消息类型字段

    /** 默认单聊 */
    private Integer sessionType = ChatConstants.SESSION_TYPE_SINGLE;  // 行注：声明会话类型字段

    /** 群聊 @全员 */
    private Boolean mentionAll = false;  // 行注：声明@提醒全部字段

    /** 群聊 @指定成员 */
    private List<Long> mentionUserIds;  // 行注：声明@提醒用户ID列表字段

    /** 幂等键，重试时携带相同值可避免重复入库 */
    @Size(max = 64, message = "clientMessageId 长度不能超过64")  // 行注：应用 @Size 注解
    private String clientMessageId;  // 行注：声明客户端消息ID字段
}  // 行注：结束当前代码块
