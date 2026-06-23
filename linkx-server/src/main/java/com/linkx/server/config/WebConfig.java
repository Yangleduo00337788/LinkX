package com.linkx.server.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 扩展点。
 * <p>
 * 历史上曾在此映射 {@code /uploads/**} 静态资源；现已关闭，文件仅通过
 * {@code GET /api/file/access/{ticket}} 在登录且 ticket 有效时返回。
 * </p>
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
}