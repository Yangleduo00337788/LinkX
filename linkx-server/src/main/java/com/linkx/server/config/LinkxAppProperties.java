package com.linkx.server.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 应用级通用配置，前缀 {@code linkx.*}。
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "linkx")
public class LinkxAppProperties {

    /** 对外 API 根地址，用于拼文件访问 URL 等 */
    private String apiBaseUrl = "http://localhost:8080";

    private final Upload upload = new Upload();

    @Getter
    @Setter
    public static class Upload {
        /** 磁盘存储根目录 */
        private String path = "uploads/";
        /** 写入数据库的 URL 前缀（实际读取走 ticket，非直链） */
        private String url = "http://localhost:8080/uploads/";
    }
}