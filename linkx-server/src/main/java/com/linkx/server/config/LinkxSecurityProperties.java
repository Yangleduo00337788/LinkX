package com.linkx.server.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "linkx.security")
public class LinkxSecurityProperties {

    private List<String> allowedOriginPatterns = new ArrayList<>(List.of(
            "http://*",
            "https://*"
    ));
}
