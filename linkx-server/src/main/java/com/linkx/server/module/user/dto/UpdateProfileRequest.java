package com.linkx.server.module.user.dto;  // 行注：声明当前文件所在包 com.linkx.server.module.user.dto

import jakarta.validation.constraints.Size;  // 行注：引入 Size 类型
import lombok.Data;  // 行注：引入 Data 类型

/** 更新本人昵称、头像、性别等（字段可选）。 */
@Data  // 行注：应用 @Data 注解
// 行注：定义 UpdateProfileRequest 类
public class UpdateProfileRequest {
    /** 昵称 */
    @Size(min = 1, max = 50, message = "昵称长度1-50位")  // 行注：应用 @Size 注解
    private String nickname;  // 行注：声明nickname字段

    /** 头像 URL */
    private String avatar;  // 行注：声明头像字段

    /** 性别 */
    private Integer gender;  // 行注：声明gender字段
}  // 行注：结束当前代码块
