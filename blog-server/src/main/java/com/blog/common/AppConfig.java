package com.blog.common;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app")
public class AppConfig {

    private JwtConfig jwt = new JwtConfig();
    private UploadConfig upload = new UploadConfig();
    private AiConfig ai = new AiConfig();

    @Data
    public static class JwtConfig {
        private String secret;
        private long expiration;
    }

    @Data
    public static class UploadConfig {
        private String path;
    }

    @Data
    public static class AiConfig {
        private String provider;
        private String apiKey;
        private String model;
        private int maxTokens;
    }
}
