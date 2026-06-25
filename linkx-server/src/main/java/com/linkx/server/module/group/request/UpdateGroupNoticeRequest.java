package com.linkx.server.module.group.request;  // 行注：声明当前文件所在包 com.linkx.server.module.group.request

import lombok.Data;  // 行注：引入 Data 类型

/** 更新群公告（管理员/群主）。 */
@Data  // 行注：应用 @Data 注解
// 行注：定义 UpdateGroupNoticeRequest 类
public class UpdateGroupNoticeRequest {
    private String notice;  // 行注：声明notice字段
}  // 行注：结束当前代码块
