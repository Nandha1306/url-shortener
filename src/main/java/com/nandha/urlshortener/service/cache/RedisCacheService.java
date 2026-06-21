package com.nandha.urlshortener.service.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nandha.urlshortener.entity.CachedUrl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Handles URL caching operations.
 *
 * Key:
 * url:abc123
 *
 * Value:
 * JSON of CachedUrl
 */
@Service
@RequiredArgsConstructor
public class RedisCacheService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Retrieves URL metadata from Redis.
     */
    public CachedUrl get(String shortCode) {
        try {
            String json = redisTemplate.opsForValue()
                    .get(buildKey(shortCode));
            if (json == null) {
                return null;
            }
            return objectMapper.readValue(json, CachedUrl.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to read cache", e);
        }
    }

    /**
     * Stores URL in cache.
     */
    public void cache(String shortCode, CachedUrl cachedUrl) {
        try {
            String json = objectMapper.writeValueAsString(cachedUrl);

            redisTemplate.opsForValue().set(
                    buildKey(shortCode),
                    json,
                    Duration.ofHours(24)
            );

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to cache URL", e);
        }
    }

    /**
     * Redis key format:
     * url:{shortCode}
     */
    private String buildKey(String shortCode) {
        return "url:" + shortCode;
    }
}