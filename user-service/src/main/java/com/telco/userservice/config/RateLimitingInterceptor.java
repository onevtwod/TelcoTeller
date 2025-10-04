package com.telco.userservice.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Map;

@Component
public class RateLimitingInterceptor implements HandlerInterceptor {

    @Autowired
    private RateLimitingConfig rateLimitingConfig;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String clientIp = getClientIpAddress(request);

        if (rateLimitingConfig.isRateLimited(clientIp)) {
            handleRateLimitExceeded(response, clientIp);
            return false;
        }

        // Add rate limit headers
        addRateLimitHeaders(response, clientIp);
        return true;
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    private void handleRateLimitExceeded(HttpServletResponse response, String clientIp) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        long timeToRefill = rateLimitingConfig.getTimeToRefill(clientIp);
        long secondsToWait = timeToRefill / 1_000_000_000; // Convert nanoseconds to seconds

        String errorResponse = String.format("{\n" +
                "    \"error\": \"Rate limit exceeded\",\n" +
                "    \"message\": \"Too many requests. Please try again in %d seconds.\",\n" +
                "    \"status\": 429,\n" +
                "    \"timestamp\": \"%s\",\n" +
                "    \"path\": \"%s\"\n" +
                "}", secondsToWait, java.time.Instant.now(), "rate-limit");

        response.getWriter().write(errorResponse);
    }

    private void addRateLimitHeaders(HttpServletResponse response, String clientIp) {
        long availableTokens = rateLimitingConfig.getAvailableTokens(clientIp);
        long timeToRefill = rateLimitingConfig.getTimeToRefill(clientIp);

        response.setHeader("X-RateLimit-Limit", "100");
        response.setHeader("X-RateLimit-Remaining", String.valueOf(availableTokens));
        response.setHeader("X-RateLimit-Reset", String.valueOf(System.currentTimeMillis() + timeToRefill / 1_000_000));
    }
}
