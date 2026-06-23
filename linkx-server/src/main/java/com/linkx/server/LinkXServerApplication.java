package com.linkx.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * LinkX IM 服务端启动入口。
 * <p>
 * 扫描 {@code com.linkx.server} 包下所有 Spring 组件（Controller、Service、配置类等），
 * 默认激活的 profile 由环境变量 {@code LINKX_PROFILE} 或 {@code application.yml} 决定（dev / prod）。
 * </p>
 */
@SpringBootApplication
public class LinkXServerApplication {

    /**
     * 启动 Spring Boot 应用。
     *
     * @param args 命令行参数（通常为空；也可通过 {@code --spring.profiles.active=prod} 覆盖 profile）
     */
    public static void main(String[] args) {
        SpringApplication.run(LinkXServerApplication.class, args);
    }
}