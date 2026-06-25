package com.linkx.server.entity;  // 行注：声明当前文件所在包 com.linkx.server.entity

import com.baomidou.mybatisplus.annotation.*;  // 行注：引入 * 类型
import lombok.Data;  // 行注：引入 Data 类型

import java.time.LocalDateTime;  // 行注：引入 LocalDateTime 类型

/** 上传文件元数据：磁盘路径、MIME、大小、上传者。 */
@Data  // 行注：应用 @Data 注解
@TableName("sys_file")  // 行注：应用 @TableName 注解
// 行注：定义 SysFile 类
public class SysFile {

    /** 主键 ID */
    @TableId(type = IdType.ASSIGN_ID)  // 行注：应用 @TableId 注解
    private Long id;  // 行注：声明ID字段

    /** 用户 ID */
    private Long userId;  // 行注：声明用户ID字段

    /** 原始文件名 */
    private String originalName;  // 行注：声明original名称字段

    /** 存储文件名 */
    private String storedName;  // 行注：声明已存储值名称字段

    /** 文件存储路径 */
    private String filePath;  // 行注：声明文件路径字段

    /** 文件访问 URL */
    private String fileUrl;  // 行注：声明文件URL字段

    /** 文件大小（字节） */
    private Long fileSize;  // 行注：声明文件大小字段

    /** 文件类型 */
    private String fileType;  // 行注：声明文件类型字段

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)  // 行注：应用 @TableField 注解
    private LocalDateTime createTime;  // 行注：声明创建时间字段
}  // 行注：结束当前代码块
