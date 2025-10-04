package com.telco.userservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class SecretManagerConfig {

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    @Bean
    public SecretManagerService secretManagerService() {
        return new SecretManagerService(activeProfile);
    }

    public static class SecretManagerService {
        private final String environment;
        private final Map<String, String> secrets = new HashMap<>();

        public SecretManagerService(String environment) {
            this.environment = environment;
            loadSecrets();
        }

        private void loadSecrets() {
            // In production, this would integrate with AWS Secrets Manager, Azure Key
            // Vault, or HashiCorp Vault
            // For now, we'll use environment variables with fallbacks

            secrets.put("database.password", getSecret("DB_PASSWORD", "telco"));
            secrets.put("jwt.secret", getSecret("JWT_SECRET", "default-jwt-secret-change-in-production"));
            secrets.put("encryption.key", getSecret("ENCRYPTION_KEY", "default-encryption-key-change-in-production"));
            secrets.put("api.key", getSecret("API_KEY", "default-api-key"));
            secrets.put("kafka.password", getSecret("KAFKA_PASSWORD", "kafka"));
            secrets.put("email.password", getSecret("EMAIL_PASSWORD", "email"));
            secrets.put("sms.api.key", getSecret("SMS_API_KEY", "sms-api-key"));
            secrets.put("monitoring.token", getSecret("MONITORING_TOKEN", "monitoring-token"));
        }

        private String getSecret(String key, String defaultValue) {
            String value = System.getenv(key);
            if (value == null || value.isEmpty()) {
                value = System.getProperty(key, defaultValue);
            }
            return value;
        }

        public String getSecret(String key) {
            return secrets.get(key);
        }

        public String getDatabasePassword() {
            return getSecret("database.password");
        }

        public String getJwtSecret() {
            return getSecret("jwt.secret");
        }

        public String getEncryptionKey() {
            return getSecret("encryption.key");
        }

        public String getApiKey() {
            return getSecret("api.key");
        }

        public String getKafkaPassword() {
            return getSecret("kafka.password");
        }

        public String getEmailPassword() {
            return getSecret("email.password");
        }

        public String getSmsApiKey() {
            return getSecret("sms.api.key");
        }

        public String getMonitoringToken() {
            return getSecret("monitoring.token");
        }

        public boolean isProduction() {
            return "prod".equals(environment);
        }

        public boolean isStaging() {
            return "staging".equals(environment);
        }

        public boolean isDevelopment() {
            return "dev".equals(environment);
        }
    }
}
