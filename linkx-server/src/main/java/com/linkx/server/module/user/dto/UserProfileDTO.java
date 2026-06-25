package com.linkx.server.module.user.dto;  // 行注：声明当前文件所在包 com.linkx.server.module.user.dto

import lombok.Data;  // 行注：引入 Data 类型

import java.time.LocalDateTime;  // 行注：引入 LocalDateTime 类型

/** 对外展示的用户资料（不含密码、邮箱等敏感字段）。 */
@Data  // 行注：应用 @Data 注解
// 行注：定义 UserProfileDTO 类
public class UserProfileDTO {
    /** 用户主键 */
    private Long id;  // 行注：声明ID字段
    /** 登录名 */
    private String username;  // 行注：声明username字段
    /** 显示昵称 */
    private String nickname;  // 行注：声明nickname字段
    /** 头像 URL */
    private String avatar;  // 行注：声明头像字段
    /** 性别编码（业务字典定义） */
    private Integer gender;  // 行注：声明gender字段
    /** 账号注册时间 */
    private LocalDateTime createTime;  // 行注：声明创建时间字段
}  // 行注：结束当前代码块
