package com.linkx.server.module.file.dto;

import org.springframework.core.io.InputStreamResource;

import java.io.InputStream;

/**
 * 从 MinIO 或本地磁盘打开的文件流及元数据。
 */
public record StoredFileContent(InputStream inputStream, String filename, long contentLength) {

    public InputStreamResource toResource() {
        return new InputStreamResource(inputStream);
    }
}