package com.nandha.urlshortener.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

/**
 * Request payload used to shorten a URL.
 */
public record ShortenRequest(

        @NotBlank(message = "Original URL is required")
        @URL(message = "Invalid URL format")
        String originalUrl,

        @Min(1)
        @Max(365)
        Integer expiresInDays
) {
}