package com.nandha.urlshortener.dto;


import java.time.LocalDateTime;

/**
 * Response returned after creating a short URL.
 */
// java automatically generates constructor, getter-setter etc..
public record ShortenResponse(
        String shortCode,
        String shortUrl,
        String originalUrl,
        LocalDateTime createdAt,
        LocalDateTime expiresAt
) {
}