package com.linkx.server.common;

import org.springframework.http.ContentDisposition;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;

/**
 * 生成 Tomcat 可接受的 Content-Disposition（中文文件名走 RFC 5987 filename*）。
 */
public final class HttpContentDispositionUtils {

    private HttpContentDispositionUtils() {
    }

    public static String build(String dispositionType, String displayFilename, String asciiFallbackFilename) {
        String type = StringUtils.hasText(dispositionType) ? dispositionType : "attachment";
        String ascii = StringUtils.hasText(asciiFallbackFilename) ? asciiFallbackFilename : "download";
        String display = StringUtils.hasText(displayFilename) ? displayFilename : ascii;
        ContentDisposition.Builder builder = ContentDisposition.builder(type).filename(ascii);
        if (!display.equals(ascii)) {
            builder.filename(display, StandardCharsets.UTF_8);
        }
        return builder.build().toString();
    }
}