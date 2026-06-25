package com.linkx.server.module.group.request;  // 行注：声明当前文件所在包 com.linkx.server.module.group.request

import jakarta.validation.constraints.NotBlank;  // 行注：引入 NotBlank 类型
import lombok.Data;  // 行注：引入 Data 类型

import java.util.ArrayList;  // 行注：引入 ArrayList 类型
import java.util.List;  // 行注：引入 List 类型

/** 创建群并可选拉入初始成员（须为操作者好友）。 */
@Data  // 行注：应用 @Data 注解
// 行注：定义 CreateGroupRequest 类
public class CreateGroupRequest {
    @NotBlank(message = "群名称不能为空")  // 行注：应用 @NotBlank 注解
    private String groupName;  // 行注：声明群名称字段

    private String groupAvatar;  // 行注：声明群头像字段

    private String notice;  // 行注：声明notice字段

    private List<Long> memberIds = new ArrayList<>();  // 行注：定义当前方法
}  // 行注：结束当前代码块
