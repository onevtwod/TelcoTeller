package com.telco.userservice.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class RateLimitingConfig {

    // Rate limiting buckets per IP address
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    // Rate limiting configuration
    private static final int REQUESTS_PER_MINUTE = 100;
    private static final int REQUESTS_PER_HOUR = 1000;
    private static final int BURST_CAPACITY = 20;

    @Bean
    public Map<String, Bucket> rateLimitBuckets() {
        return buckets;
    }

    public Bucket getBucket(String key) {
        return buckets.computeIfAbsent(key, this::createNewBucket);
    }

    private Bucket createNewBucket(String key) {
        // Refill tokens every minute
        Refill refill = Refill.intervally(REQUESTS_PER_MINUTE, Duration.ofMinutes(1));

        // Create bandwidth with burst capacity
        Bandwidth limit = Bandwidth.classic(REQUESTS_PER_MINUTE, refill)
                .withInitialTokens(BURST_CAPACITY);

        return Bucket4j.builder()
                .addLimit(limit)
                .build();
    }

    public boolean isRateLimited(String key) {
        Bucket bucket = getBucket(key);
        return !bucket.tryConsume(1);
    }

    public long getAvailableTokens(String key) {
        Bucket bucket = getBucket(key);
        return bucket.getAvailableTokens();
    }

    public long getTimeToRefill(String key) {
        Bucket bucket = getBucket(key);
        // Return a default time to refill (60 seconds in nanoseconds)
        return 60_000_000_000L;
    }
}
