package com.nandha.urlshortener.dto;

import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;

/**
 * Metadata about a shortened URL.
 */
public record UrlStatsResponse (
    String shortCode,
    String originalUrl,
    Long clickCount,
    LocalDateTime createdAt,
    LocalDateTime expireAt
){
}