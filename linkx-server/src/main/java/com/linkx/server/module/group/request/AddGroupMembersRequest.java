package com.linkx.server.module.group.request;  // 行注：声明当前文件所在包 com.linkx.server.module.group.request

import jakarta.validation.constraints.NotEmpty;  // 行注：引入 NotEmpty 类型
import lombok.Data;  // 行注：引入 Data 类型

import java.util.ArrayList;  // 行注：引入 ArrayList 类型
import java.util.List;  // 行注：引入 List 类型

/** 群主/管理员直接拉好友入群。 */
@Data  // 行注：应用 @Data 注解
// 行注：定义 AddGroupMembersRequest 类
public class AddGroupMembersRequest {
    @NotEmpty(message = "请选择要添加的成员")  // 行注：应用 @NotEmpty 注解
    private List<Long> memberIds = new ArrayList<>();  // 行注：定义当前方法
}  // 行注：结束当前代码块
