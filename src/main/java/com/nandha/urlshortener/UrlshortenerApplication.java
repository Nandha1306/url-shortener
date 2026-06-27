package com.nandha.urlshortener;

import com.nandha.urlshortener.config.RateLimitProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@EnableConfigurationProperties(RateLimitProperties.class)
@ConfigurationPropertiesScan
public class UrlshortenerApplication {
	public static void main(String[] args) {
		SpringApplication.run(UrlshortenerApplication.class, args);
	}
}