package com.linkx.server.module.group.request;  // 行注：声明当前文件所在包 com.linkx.server.module.group.request

import jakarta.validation.constraints.NotNull;  // 行注：引入 NotNull 类型
import lombok.Data;  // 行注：引入 Data 类型

/** 用户主动申请加入某群。 */
@Data  // 行注：应用 @Data 注解
// 行注：定义 GroupJoinRequest 类
public class GroupJoinRequest {
    @NotNull(message = "请输入群ID")  // 行注：应用 @NotNull 注解
    private Long groupId;  // 行注：声明群ID字段

    private String message;  // 行注：声明消息字段
}  // 行注：结束当前代码块
