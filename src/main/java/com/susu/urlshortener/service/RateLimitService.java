package com.susu.urlshortener.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RateLimitService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final int MAX_REQUESTS = 5;
    private static final long WINDOW_SECONDS = 60;

    public RateLimitService(
            RedisTemplate<String, String> redisTemplate
    ) {
        this.redisTemplate = redisTemplate;
    }

    public boolean isAllowed(
            String endpoint,
            String ipAddress
    ) {

        String key =
                "rate_limit:" + endpoint + ":" + ipAddress;

        Long count =
                redisTemplate.opsForValue().increment(key);

        if (count == null) {
            return false;
        }

        if (count == 1) {
            redisTemplate.expire(
                    key,
                    Duration.ofSeconds(WINDOW_SECONDS)
            );
        }

        return count <= MAX_REQUESTS;
    }
}