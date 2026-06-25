package com.linkx.server.config;  // 行注：声明当前文件所在包 com.linkx.server.config

import com.baomidou.mybatisplus.annotation.DbType;  // 行注：引入 DbType 类型
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;  // 行注：引入 MetaObjectHandler 类型
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;  // 行注：引入 MybatisPlusInterceptor 类型
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;  // 行注：引入 PaginationInnerInterceptor 类型
import org.apache.ibatis.reflection.MetaObject;  // 行注：引入 MetaObject 类型
import org.springframework.context.annotation.Bean;  // 行注：引入 Bean 类型
import org.springframework.context.annotation.Configuration;  // 行注：引入 Configuration 类型

import java.time.LocalDateTime;  // 行注：引入 LocalDateTime 类型

/**
 * MyBatis-Plus：MySQL 分页插件与 {@code createTime}/{@code updateTime} 自动填充。
 */
@Configuration  // 行注：应用 @Configuration 注解
// 行注：定义 MyBatisPlusConfig 类
public class MyBatisPlusConfig {

    /** 注册分页插件，使 Mapper 的 Page 查询在 MySQL 上生成 LIMIT */
    @Bean  // 行注：应用 @Bean 注解
    // 行注：定义mybatisPlus拦截器方法
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();  // 行注：初始化拦截器
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));  // 行注：调用添加Inner拦截器
        return interceptor;  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /**
     * 插入/更新时自动写入时间字段（实体须标注 {@code @TableField(fill = ...)}）。
     */
    @Bean  // 行注：应用 @Bean 注解
    // 行注：定义元数据Object处理器方法
    public MetaObjectHandler metaObjectHandler() {
        return new MetaObjectHandler() {  // 行注：返回处理结果
            /** 新建记录时填充创建时间与更新时间 */
            @Override  // 行注：应用 @Override 注解
            // 行注：定义insertFill方法
            public void insertFill(MetaObject metaObject) {
                this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());  // 行注：调用strictInsertFill
                this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());  // 行注：调用strictInsertFill
            }  // 行注：结束当前代码块

            /** 更新记录时刷新更新时间 */
            @Override  // 行注：应用 @Override 注解
            // 行注：定义更新Fill方法
            public void updateFill(MetaObject metaObject) {
                this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());  // 行注：调用strict更新Fill
            }  // 行注：结束当前代码块
        };  // 行注：结束当前代码块
    }  // 行注：结束当前代码块
}  // 行注：结束当前代码块
