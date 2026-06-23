package com.linkx.server.module.file.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/** 签发文件访问 ticket 的响应（含带 ticket 的 accessUrl）。 */
@Data
@AllArgsConstructor
public class FileAccessDTO {

    private String accessUrl;
}
