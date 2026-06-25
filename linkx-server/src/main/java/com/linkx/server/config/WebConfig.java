package com.linkx.server.config;  // 行注：声明当前文件所在包 com.linkx.server.config

import org.springframework.context.annotation.Configuration;  // 行注：引入 Configuration 类型
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;  // 行注：引入 WebMvcConfigurer 类型

/**
 * Web MVC 扩展点。
 * <p>
 * 历史上曾在此映射 {@code /uploads/**} 静态资源；现已关闭，文件仅通过
 * {@code GET /api/file/access/{ticket}} 在登录且 ticket 有效时返回。
 * </p>
 */
@Configuration  // 行注：应用 @Configuration 注解
// 行注：定义 WebConfig 类
public class WebConfig implements WebMvcConfigurer {
}  // 行注：结束当前代码块
