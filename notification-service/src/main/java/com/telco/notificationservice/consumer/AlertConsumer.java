package com.telco.notificationservice.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class AlertConsumer {
    @KafkaListener(topics = "sms-alerts")
    public void consume(ConsumerRecord<String, String> record) {
        String key = record.key();
        String value = record.value();
        System.out.println("INFO: SMS sent to user " + key + ": " + value);
    }
}
