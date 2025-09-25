package com.telco.alerttriggerservice.scheduler;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
public class UsageCheckScheduler {
    private final RestTemplate restTemplate = new RestTemplate();
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${user.service.base-url}")
    private String userServiceBaseUrl;

    @Value("${alert.threshold.percent}")
    private int thresholdPercent;

    public UsageCheckScheduler(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    // Every 15 seconds
    @Scheduled(fixedDelay = 15000, initialDelay = 5000)
    public void checkUsers() {
        try {
            ResponseEntity<List> response = restTemplate.getForEntity(userServiceBaseUrl + "/users", List.class);
            List<Map<String, Object>> users = response.getBody();
            if (users == null)
                return;
            for (Map<String, Object> user : users) {
                String userId = (String) user.get("userId");
                String phone = (String) user.get("phoneNumber");
                Number limit = (Number) user.get("dataPlanLimit");
                Number usage = (Number) user.get("currentUsage");
                if (limit == null || usage == null)
                    continue;
                double percent = (usage.doubleValue() / Math.max(limit.doubleValue(), 1.0)) * 100.0;
                if (percent >= thresholdPercent) {
                    String msg = String.format("{\"userId\":\"%s\",\"phoneNumber\":\"%s\",\"percent\":%.0f}", userId,
                            phone, percent);
                    kafkaTemplate.send("sms-alerts", userId, msg);
                }
            }
        } catch (Exception ignored) {
        }
    }
}
