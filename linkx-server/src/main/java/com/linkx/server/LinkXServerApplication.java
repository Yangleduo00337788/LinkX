package com.linkx.server;  // 行注：声明当前文件所在包 com.linkx.server

import org.springframework.boot.SpringApplication;  // 行注：引入 SpringApplication 类型
import org.springframework.boot.autoconfigure.SpringBootApplication;  // 行注：引入 SpringBootApplication 类型

/**
 * LinkX IM 服务端启动入口。
 * <p>
 * 扫描 {@code com.linkx.server} 包下所有 Spring 组件（Controller、Service、配置类等），
 * 默认激活的 profile 由环境变量 {@code LINKX_PROFILE} 或 {@code application.yml} 决定（dev / prod）。
 * </p>
 */
@SpringBootApplication  // 行注：应用 @SpringBootApplication 注解
// 行注：定义 LinkXServerApplication 类
public class LinkXServerApplication {

    /**
     * 启动 Spring Boot 应用。
     *
     * @param args 命令行参数（通常为空；也可通过 {@code --spring.profiles.active=prod} 覆盖 profile）
     */
    // 行注：定义main方法
    public static void main(String[] args) {
        // 统一从这里进入 Spring 生命周期：加载配置、创建容器、注册 Bean、启动内嵌 Web 服务器。
        SpringApplication.run(LinkXServerApplication.class, args);  // 行注：调用run
    }  // 行注：结束当前代码块
}  // 行注：结束当前代码块
