package com.linkx.server.config;  // 行注：声明当前文件所在包 com.linkx.server.config

import lombok.Getter;  // 行注：引入 Getter 类型
import lombok.Setter;  // 行注：引入 Setter 类型
import org.springframework.boot.context.properties.ConfigurationProperties;  // 行注：引入 ConfigurationProperties 类型
import org.springframework.stereotype.Component;  // 行注：引入 Component 类型

/**
 * 应用级通用配置，前缀 {@code linkx.*}。
 */
@Getter  // 行注：应用 @Getter 注解
@Setter  // 行注：应用 @Setter 注解
@Component  // 行注：应用 @Component 注解
@ConfigurationProperties(prefix = "linkx")  // 行注：应用 @ConfigurationProperties 注解
// 行注：定义 LinkxAppProperties 类
public class LinkxAppProperties {

    /** 对外 API 根地址，用于拼文件访问 URL 等 */
    private String apiBaseUrl = "http://localhost:8080";  // 行注：声明接口基础URL字段

    private final Upload upload = new Upload();  // 行注：注入上传依赖

    @Getter  // 行注：应用 @Getter 注解
    @Setter  // 行注：应用 @Setter 注解
    // 行注：定义 Upload 类
    public static class Upload {
        /** 磁盘存储根目录 */
        private String path = "uploads/";  // 行注：声明路径字段
        /** 写入数据库的 URL 前缀（实际读取走 ticket，非直链） */
        private String url = "http://localhost:8080/uploads/";  // 行注：声明URL字段
    }  // 行注：结束当前代码块
}  // 行注：结束当前代码块
