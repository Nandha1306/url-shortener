package com.nandha.urlshortener.dto;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

/**
 * Request payload used to shorten a URL.
 */
public record ShortenRequest(

        @NotBlank(message = "Original URL is required")
        @URL(message = "Invalid URL format")
        String originalUrl
) {
}