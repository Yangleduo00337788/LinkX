package com.linkx.server.module.file.dto;  // 行注：声明当前文件所在包 com.linkx.server.module.file.dto

import lombok.Data;  // 行注：引入 Data 类型

import java.time.LocalDateTime;  // 行注：引入 LocalDateTime 类型

/** 上传结果与文件列表项（url 多为需换 ticket 的存储路径）。 */
@Data  // 行注：应用 @Data 注解
// 行注：定义 FileDTO 类
public class FileDTO {

    /** 主键 ID */
    private Long id;  // 行注：声明ID字段

    /** 原始文件名 */
    private String originalName;  // 行注：声明original名称字段

    /** 文件访问 URL */
    private String fileUrl;  // 行注：声明文件URL字段

    /** 文件大小（字节） */
    private Long fileSize;  // 行注：声明文件大小字段

    /** 文件类型 */
    private String fileType;  // 行注：声明文件类型字段

    /** 创建时间 */
    private LocalDateTime createTime;  // 行注：声明创建时间字段
}  // 行注：结束当前代码块
