package com.nandha.urlshortener.dto;

/**
 * Response returned after URL shortening.
 */
// java automatically generates constructor, getter-setter etc..
public record ShortenResponse(
        String shortCode,
        String shortUrl,
        String originalUrl
) {
}