package com.linkx.server.config;  // 行注：声明当前文件所在包 com.linkx.server.config

import com.fasterxml.jackson.core.JsonGenerator;  // 行注：引入 JsonGenerator 类型
import com.fasterxml.jackson.databind.JsonSerializer;  // 行注：引入 JsonSerializer 类型
import com.fasterxml.jackson.databind.SerializerProvider;  // 行注：引入 SerializerProvider 类型
import com.fasterxml.jackson.databind.module.SimpleModule;  // 行注：引入 SimpleModule 类型
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;  // 行注：引入 JavaTimeModule 类型
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;  // 行注：引入 LocalDateTimeDeserializer 类型
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;  // 行注：引入 LocalDateTimeSerializer 类型
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;  // 行注：引入 Jackson2ObjectMapperBuilderCustomizer 类型
import org.springframework.context.annotation.Bean;  // 行注：引入 Bean 类型
import org.springframework.context.annotation.Configuration;  // 行注：引入 Configuration 类型

import java.io.IOException;  // 行注：引入 IOException 类型
import java.time.LocalDateTime;  // 行注：引入 LocalDateTime 类型
import java.time.format.DateTimeFormatter;  // 行注：引入 DateTimeFormatter 类型

/**
 * JSON 序列化定制：时间格式、Long 防前端精度丢失。
 */
@Configuration  // 行注：应用 @Configuration 注解
// 行注：定义 JacksonConfig 类
public class JacksonConfig {

    /**
     * 配置 Jackson 序列化与反序列化行为。
     *
     * @return Jackson 配置定制器
     */
    @Bean  // 行注：应用 @Bean 注解
    // 行注：定义JacksonCustomizer方法
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> {  // 行注：返回处理结果
            JavaTimeModule javaTimeModule = new JavaTimeModule();  // 行注：初始化java时间模块
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");  // 行注：初始化formatter
            javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(formatter));  // 行注：调用添加Serializer
            javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(formatter));  // 行注：调用添加Deserializer

            SimpleModule module = new SimpleModule();  // 行注：初始化模块
            module.addSerializer(Long.class, new SafeLongSerializer());  // 行注：调用添加Serializer
            builder.modules(module, javaTimeModule);  // 行注：调用modules
        };  // 行注：结束当前代码块
    }  // 行注：结束当前代码块

    /**
     * 超过 JS Number.MAX_SAFE_INTEGER 的 Long 序列化为字符串，避免前端 ID 错乱。
     */
    // 行注：定义 SafeLongSerializer 类
    private static class SafeLongSerializer extends JsonSerializer<Long> {
        private static final long MAX_SAFE = 9007199254740991L;  // 行注：定义最大SAFE常量

        /**
         * 将 Long 类型序列化为字符串，避免前端精度丢失。
         *
         * @param value 待处理值
         * @param gen JSON 生成器
         * @param provider 序列化上下文
         */
        @Override  // 行注：应用 @Override 注解
        // 行注：定义serialize方法
        public void serialize(Long value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            // 行注：判断是否满足当前条件
            if (value != null && (value > MAX_SAFE || value < -MAX_SAFE)) {
                gen.writeString(value.toString());  // 行注：调用write字符串
            // 行注：开始当前语句对应的代码块
            } else {
                gen.writeNumber(value);  // 行注：调用writeNumber
            }  // 行注：结束当前代码块
        }  // 行注：结束当前代码块
    }  // 行注：结束当前代码块
}  // 行注：结束当前代码块
