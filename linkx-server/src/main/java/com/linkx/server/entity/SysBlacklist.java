package com.linkx.server.entity;  // 行注：声明当前文件所在包 com.linkx.server.entity

import com.baomidou.mybatisplus.annotation.*;  // 行注：引入 * 类型
import lombok.Data;  // 行注：引入 Data 类型

import java.time.LocalDateTime;  // 行注：引入 LocalDateTime 类型

/** 用户拉黑记录：拉黑后不可发消息/加好友等。 */
@Data  // 行注：应用 @Data 注解
@TableName("sys_blacklist")  // 行注：应用 @TableName 注解
// 行注：定义 SysBlacklist 类
public class SysBlacklist {

    /** 主键 ID */
    @TableId(type = IdType.ASSIGN_ID)  // 行注：应用 @TableId 注解
    private Long id;  // 行注：声明ID字段

    /** 用户 ID */
    private Long userId;  // 行注：声明用户ID字段

    /** 被拉黑用户 ID */
    private Long blacklistUserId;  // 行注：声明黑名单用户ID字段

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)  // 行注：应用 @TableField 注解
    private LocalDateTime createTime;  // 行注：声明创建时间字段
}  // 行注：结束当前代码块
