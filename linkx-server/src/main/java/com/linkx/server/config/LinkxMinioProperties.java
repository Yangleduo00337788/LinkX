package com.linkx.server.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * MinIO 对象存储配置，绑定 {@code linkx.minio.*}。
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "linkx.minio")
public class LinkxMinioProperties {

    /** 是否启用 MinIO（false 时仍使用本地磁盘 {@link LinkxAppProperties.Upload}） */
    private boolean enabled = true;

    private String endpoint = "http://127.0.0.1:9000";

    private String accessKey = "minioadmin";

    private String secretKey = "minioadmin123";

    /** 存储桶名称，启动时会自动创建（若不存在） */
    private String bucket = "linkx";
}