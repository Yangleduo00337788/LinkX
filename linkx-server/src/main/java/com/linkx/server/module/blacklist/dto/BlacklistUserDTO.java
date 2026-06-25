package com.linkx.server.module.blacklist.dto;  // 行注：声明当前文件所在包 com.linkx.server.module.blacklist.dto

import lombok.Data;  // 行注：引入 Data 类型

/** 黑名单列表中的用户摘要。 */
@Data  // 行注：应用 @Data 注解
// 行注：定义 BlacklistUserDTO 类
public class BlacklistUserDTO {

    /** 主键 ID */
    private Long id;  // 行注：声明ID字段

    /** 用户名 */
    private String username;  // 行注：声明username字段

    /** 昵称 */
    private String nickname;  // 行注：声明nickname字段

    /** 头像 URL */
    private String avatar;  // 行注：声明头像字段
}  // 行注：结束当前代码块
