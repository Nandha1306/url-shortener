package com.nandha.urlshortener.util;

/**
 * Redis key prefixes used throughout the application.
 */
public final class RedisKeys {
    public static final String URL_PREFIX = "url:";
    public static final String RATE_LIMIT_PREFIX = "ratelimit:";

    private RedisKeys() {
    }
}