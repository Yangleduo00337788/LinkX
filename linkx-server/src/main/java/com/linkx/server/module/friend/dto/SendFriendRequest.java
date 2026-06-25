package com.linkx.server.module.friend.dto;  // 行注：声明当前文件所在包 com.linkx.server.module.friend.dto

import jakarta.validation.constraints.NotNull;  // 行注：引入 NotNull 类型
import lombok.Data;  // 行注：引入 Data 类型

/** 发送好友申请。 */
@Data  // 行注：应用 @Data 注解
// 行注：定义 SendFriendRequest 类
public class SendFriendRequest {
    /** 目标用户 ID */
    @NotNull(message = "用户ID不能为空")  // 行注：应用 @NotNull 注解
    private Long toUserId;  // 行注：声明转为用户ID字段

    /** 申请或消息附言 */
    private String message;  // 行注：声明消息字段
}  // 行注：结束当前代码块
