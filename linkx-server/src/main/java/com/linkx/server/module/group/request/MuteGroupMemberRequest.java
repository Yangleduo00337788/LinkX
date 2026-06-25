package com.linkx.server.module.group.request;  // 行注：声明当前文件所在包 com.linkx.server.module.group.request

import jakarta.validation.constraints.Max;  // 行注：引入 Max 类型
import jakarta.validation.constraints.Min;  // 行注：引入 Min 类型
import jakarta.validation.constraints.NotNull;  // 行注：引入 NotNull 类型
import lombok.Data;  // 行注：引入 Data 类型

/** 设置群成员禁言时长。 */
@Data  // 行注：应用 @Data 注解
// 行注：定义 MuteGroupMemberRequest 类
public class MuteGroupMemberRequest {
    @NotNull(message = "禁言时长不能为空")  // 行注：应用 @NotNull 注解
    @Min(value = 1, message = "禁言时长至少为1分钟")  // 行注：应用 @Min 注解
    @Max(value = 43200, message = "禁言时长不能超过43200分钟")  // 行注：应用 @Max 注解
    private Integer muteMinutes;  // 行注：声明muteMinutes字段
}  // 行注：结束当前代码块
