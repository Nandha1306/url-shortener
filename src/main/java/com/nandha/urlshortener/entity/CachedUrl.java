package com.nandha.urlshortener.entity;

public record CachedUrl(
        Long id,
        String originalUrl
) {}
