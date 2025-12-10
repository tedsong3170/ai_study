package com.stock.chat.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "kis.api")
public class KisApiProperties {
    private String appKey;
    private String appSecret;
    private String baseUrl;
}
