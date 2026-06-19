package com.linkx.server.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final LinkxAppProperties linkxAppProperties;

    public WebConfig(LinkxAppProperties linkxAppProperties) {
        this.linkxAppProperties = linkxAppProperties;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadPath = linkxAppProperties.getUpload().getPath();
        String normalizedPath = uploadPath.endsWith("/") ? uploadPath : uploadPath + "/";
        registry.addResourceHandler("/uploads/avatar/**")
                .addResourceLocations("file:" + normalizedPath + "avatar/");
    }
}
