package com.nandha.urlshortener.dto;

import java.time.LocalDateTime;

public record CachedUrl(
        Long id,
        String originalUrl,
        LocalDateTime expiresAt
) {}
