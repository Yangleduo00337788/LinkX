package com.linkx.server.module.file.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FileDTO {

    private Long id;

    private String originalName;

    private String fileUrl;

    private Long fileSize;

    private String fileType;

    private LocalDateTime createTime;
}
