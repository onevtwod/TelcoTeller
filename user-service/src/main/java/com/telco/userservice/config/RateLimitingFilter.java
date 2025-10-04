package com.telco.userservice.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RateLimitingFilter implements Filter {

    @Autowired
    private RateLimitingConfig rateLimitingConfig;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String clientIp = getClientIpAddress(httpRequest);

        if (rateLimitingConfig.isRateLimited(clientIp)) {
            handleRateLimitExceeded(httpResponse, clientIp);
            return;
        }

        // Add rate limit headers
        addRateLimitHeaders(httpResponse, clientIp);
        chain.doFilter(request, response);
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
