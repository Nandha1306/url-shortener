package com.nandha.urlshortener.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.cache")
@Getter
@Setter
public class CacheProperties {
    private long urlTtlHours;
}
