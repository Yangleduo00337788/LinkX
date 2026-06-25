package com.linkx.server.module.chat.dto;  // 行注：声明当前文件所在包 com.linkx.server.module.chat.dto

import lombok.AllArgsConstructor;  // 行注：引入 AllArgsConstructor 类型
import lombok.Data;  // 行注：引入 Data 类型

/** GET /api/chat/ws-ticket 返回，用于连接 /ws/chat?ticket= */
@Data  // 行注：应用 @Data 注解
@AllArgsConstructor  // 行注：应用 @AllArgsConstructor 注解
// 行注：定义 ChatWsTicketDTO 类
public class ChatWsTicketDTO {

    /** 短期访问票据 */
    private String ticket;  // 行注：声明票据字段
}  // 行注：结束当前代码块
