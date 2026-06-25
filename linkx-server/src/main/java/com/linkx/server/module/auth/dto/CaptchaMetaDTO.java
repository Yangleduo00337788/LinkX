package com.linkx.server.module.auth.dto;  // 行注：声明当前文件所在包 com.linkx.server.module.auth.dto

import lombok.AllArgsConstructor;  // 行注：引入 AllArgsConstructor 类型
import lombok.Data;  // 行注：引入 Data 类型

import java.util.List;  // 行注：引入 List 类型

/** 前端登录/注册页用于判断是否展示验证码及可用 scene。 */
@Data  // 行注：应用 @Data 注解
@AllArgsConstructor  // 行注：应用 @AllArgsConstructor 注解
// 行注：定义 CaptchaMetaDTO 类
public class CaptchaMetaDTO {
    /** 是否启用 */
    private boolean enabled;  // 行注：声明启用字段
    /** 通常为 login、register */
    private List<String> scenes;  // 行注：声明scenes字段
}  // 行注：结束当前代码块
