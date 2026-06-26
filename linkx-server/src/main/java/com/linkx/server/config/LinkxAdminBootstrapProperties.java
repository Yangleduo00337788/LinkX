package com.linkx.server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 首次启动无管理员时，按配置创建默认超级管理员（仅当 sys_admin 为空）。
 */
@Data
@Component
@ConfigurationProperties(prefix = "linkx.admin.bootstrap")
public class LinkxAdminBootstrapProperties {

    /** 是否启用自动创建 */
    private boolean enabled = true;

    private String defaultUsername = "admin";

  /** 生产环境务必通过环境变量覆盖 */
    private String defaultPassword = "LinkX@Admin2026";

    private String defaultDisplayName = "系统管理员";
}