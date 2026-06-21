package com.nandha.urlshortener.dto;

import java.time.LocalDateTime;

/**
 * Metadata about a shortened URL.
 */
public record UrlStatsResponse (
    String shortCode,
    String originalUrl,
    Long clickCount,
    LocalDateTime createdAt
){
}