package com.telco.userservice.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class CaptchaConfig {

    @Autowired
    private SecretManagerConfig.SecretManagerService secretManagerService;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public CaptchaService captchaService() {
        return new CaptchaService(secretManagerService, restTemplate());
    }

    public static class CaptchaService {
        private final SecretManagerConfig.SecretManagerService secretManager;
        private final RestTemplate restTemplate;

        // reCAPTCHA v3 endpoints
        private static final String RECAPTCHA_VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";
        private static final double MINIMUM_SCORE = 0.5; // Minimum score for reCAPTCHA v3

        public CaptchaService(SecretManagerConfig.SecretManagerService secretManager, RestTemplate restTemplate) {
            this.secretManager = secretManager;
            this.restTemplate = restTemplate;
        }

        public boolean verifyCaptcha(String captchaToken, String clientIp) {
            // Skip CAPTCHA verification in development
            if (secretManager.isDevelopment()) {
                return true;
            }

            try {
                String secretKey = secretManager.getSecret("captcha.secret.key");
                if (secretKey == null || secretKey.isEmpty()) {
                    // If no secret key is configured, allow the request (for development)
                    return true;
                }

                String url = String.format("%s?secret=%s&response=%s&remoteip=%s",
                        RECAPTCHA_VERIFY_URL, secretKey, captchaToken, clientIp);

                CaptchaResponse response = restTemplate.postForObject(url, null, CaptchaResponse.class);

                if (response != null && response.isSuccess()) {
                    // For reCAPTCHA v3, check the score
                    return response.getScore() >= MINIMUM_SCORE;
                }

                return false;
            } catch (Exception e) {
                // Log the error and fail securely
                System.err.println("CAPTCHA verification failed: " + e.getMessage());
                return false;
            }
        }

        public boolean isCaptchaRequired() {
            // Require CAPTCHA in production and staging
            return secretManager.isProduction() || secretManager.isStaging();
        }

        public String getCaptchaSiteKey() {
            return secretManager.getSecret("captcha.site.key");
        }

        public static class CaptchaResponse {
            private boolean success;
            private double score;
            private String action;
            private String challenge_ts;
            private String hostname;
            private String[] errorCodes;

            // Getters and setters
            public boolean isSuccess() {
                return success;
            }

            public void setSuccess(boolean success) {
                this.success = success;
            }

            public double getScore() {
                return score;
            }

            public void setScore(double score) {
                this.score = score;
            }

            public String getAction() {
                return action;
            }

            public void setAction(String action) {
                this.action = action;
            }

            public String getChallenge_ts() {
                return challenge_ts;
            }

            public void setChallenge_ts(String challenge_ts) {
                this.challenge_ts = challenge_ts;
            }

            public String getHostname() {
                return hostname;
            }

            public void setHostname(String hostname) {
                this.hostname = hostname;
            }

            public String[] getErrorCodes() {
                return errorCodes;
            }

            public void setErrorCodes(String[] errorCodes) {
                this.errorCodes = errorCodes;
            }
        }
    }
}
