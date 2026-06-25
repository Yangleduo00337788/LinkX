package com.linkx.server.module.group.request;  // 行注：声明当前文件所在包 com.linkx.server.module.group.request

import jakarta.validation.constraints.Size;  // 行注：引入 Size 类型
import lombok.Data;  // 行注：引入 Data 类型

/** 当前用户在群内的备注、消息免打扰等偏好。 */
@Data  // 行注：应用 @Data 注解
// 行注：定义 UpdateGroupPreferencesRequest 类
public class UpdateGroupPreferencesRequest {

    @Size(max = 100, message = "群备注长度不能超过100个字符")  // 行注：应用 @Size 注解
    private String groupRemark;  // 行注：声明群Remark字段

    private Boolean notificationMuted;  // 行注：声明通知Muted字段
}  // 行注：结束当前代码块
