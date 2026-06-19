package com.linkx.server.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "linkx")
public class LinkxAppProperties {

    private String apiBaseUrl = "http://localhost:8080";

    private final Upload upload = new Upload();

    @Getter
    @Setter
    public static class Upload {
        private String path = "uploads/";
        private String url = "http://localhost:8080/uploads/";
    }
}
