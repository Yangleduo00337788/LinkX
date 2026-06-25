package com.linkx.server.module.file.dto;  // 行注：声明当前文件所在包 com.linkx.server.module.file.dto

import lombok.AllArgsConstructor;  // 行注：引入 AllArgsConstructor 类型
import lombok.Data;  // 行注：引入 Data 类型

/** 签发文件访问 ticket 的响应（含带 ticket 的 accessUrl）。 */
@Data  // 行注：应用 @Data 注解
@AllArgsConstructor  // 行注：应用 @AllArgsConstructor 注解
// 行注：定义 FileAccessDTO 类
public class FileAccessDTO {

    /** 带票据的临时访问地址 */
    private String accessUrl;  // 行注：声明访问URL字段
}  // 行注：结束当前代码块
