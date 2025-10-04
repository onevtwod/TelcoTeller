package com.telco.userservice.saga.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.telco.userservice.saga.event.SagaEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class SagaEventProducer {

    private static final Logger logger = LoggerFactory.getLogger(SagaEventProducer.class);
    private static final String SAGA_TOPIC = "saga-events";

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public void publishSagaEvent(SagaEvent event) {
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            String key = event.getSagaId() != null ? event.getSagaId() : event.getEventId();

            logger.info("Publishing saga event: {} for saga: {}", event.getEventType(), event.getSagaId());

            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(SAGA_TOPIC, key, eventJson);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    logger.info("Successfully published saga event: {} with offset: {}",
                            event.getEventType(), result.getRecordMetadata().offset());
                } else {
                    logger.error("Failed to publish saga event: {} for saga: {}",
                            event.getEventType(), event.getSagaId(), ex);
                }
            });

        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize saga event: {} for saga: {}",
                    event.getEventType(), event.getSagaId(), e);
        }
    }

    public void publishSagaEvent(String topic, SagaEvent event) {
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            String key = event.getSagaId() != null ? event.getSagaId() : event.getEventId();

            logger.info("Publishing saga event: {} to topic: {} for saga: {}",
                    event.getEventType(), topic, event.getSagaId());

            kafkaTemplate.send(topic, key, eventJson);

        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize saga event: {} for saga: {}",
                    event.getEventType(), event.getSagaId(), e);
        }
    }
}
