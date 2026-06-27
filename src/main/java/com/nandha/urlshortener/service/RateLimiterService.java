package com.nandha.urlshortener.service;

import com.nandha.urlshortener.config.RateLimitProperties;
import com.nandha.urlshortener.exception.RateLimitExceededException;
import com.nandha.urlshortener.util.RedisKeys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

//@EnableConfigurationProperties(RateLimitProperties.class)
@Service
@RequiredArgsConstructor
@Slf4j
/**
 * Implements a Fixed Window rate limiter using Redis.
 *
 * Example:
 * 10 requests per 60 seconds.
 */
public class RateLimiterService {

    private final StringRedisTemplate redisTemplate;
    private final RateLimitProperties properties;

    /**
     * Verifies whether a client
     * can perform another request.
     */
    public void checkLimit(String clientIp){
        String key = "ratelimit:" + clientIp;
        Long cnt =
                redisTemplate.opsForValue()
                        .increment(key);

        // output: ratelimit: count=1 key=ratelimit:0:0:0:0:0:0:0:1
        log.debug("{} count={} key={}", RedisKeys.RATE_LIMIT_PREFIX, cnt, key);

        // if cnt == 1 means, it is the first request. hence it will start the TTL
        if(cnt != null && cnt == 1) {
            redisTemplate.expire(
                    key,
                    Duration.ofSeconds(
                            properties.getWindowSeconds()
                    )
            );
        }
        log.debug("Rate limit count={}", cnt);

        if(cnt != null && cnt > properties.getMaxRequests()) {
            System.out.println("ratelimit exceeded");
            throw new RateLimitExceededException();
        }
    }
}
