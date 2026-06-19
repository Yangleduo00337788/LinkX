package com.linkx.server.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "linkx.security")
public class LinkxSecurityProperties {

    private List<String> allowedOriginPatterns = new ArrayList<>();
    private final AuthRateLimit authRateLimit = new AuthRateLimit();
    private final Captcha captcha = new Captcha();

    public List<String> getAllowedOriginPatterns() {
        return allowedOriginPatterns.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .collect(Collectors.toList());
    }

    @Getter
    @Setter
    public static class AuthRateLimit {
        private boolean enabled = true;
        private long windowSeconds = 60;
        private long loginMaxRequests = 10;
        private long registerMaxRequests = 5;
        private long refreshMaxRequests = 20;
    }

    @Getter
    @Setter
    public static class Captcha {
        private boolean enabled = false;
    }
}
