package com.linkx.server.module.group.request;  // 行注：声明当前文件所在包 com.linkx.server.module.group.request

import jakarta.validation.constraints.NotBlank;  // 行注：引入 NotBlank 类型
import lombok.Data;  // 行注：引入 Data 类型

/** 修改群名称、头像等资料。 */
@Data  // 行注：应用 @Data 注解
// 行注：定义 UpdateGroupProfileRequest 类
public class UpdateGroupProfileRequest {
    @NotBlank(message = "群名称不能为空")  // 行注：应用 @NotBlank 注解
    private String groupName;  // 行注：声明群名称字段

    private String groupAvatar;  // 行注：声明群头像字段
}  // 行注：结束当前代码块
