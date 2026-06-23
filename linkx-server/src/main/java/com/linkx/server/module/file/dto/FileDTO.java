package com.linkx.server.module.file.dto;

import lombok.Data;

import java.time.LocalDateTime;

/** 上传结果与文件列表项（url 多为需换 ticket 的存储路径）。 */
@Data
public class FileDTO {

    private Long id;

    private String originalName;

    private String fileUrl;

    private Long fileSize;

    private String fileType;

    private LocalDateTime createTime;
}
