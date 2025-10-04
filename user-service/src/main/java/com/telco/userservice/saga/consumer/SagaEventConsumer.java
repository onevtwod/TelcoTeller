package com.telco.userservice.saga.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.telco.userservice.saga.event.SagaEvent;
import com.telco.userservice.saga.orchestrator.SagaOrchestrator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class SagaEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(SagaEventConsumer.class);

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SagaOrchestrator sagaOrchestrator;

    @KafkaListener(topics = "saga-events", groupId = "user-service-saga-consumer")
    public void handleSagaEvent(@Payload String eventJson,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header("kafka_receivedPartitionId") int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        try {
            logger.info("Received saga event from topic: {}, partition: {}, offset: {}", topic, partition, offset);

            SagaEvent event = objectMapper.readValue(eventJson, SagaEvent.class);
            logger.info("Processing saga event: {} for saga: {}", event.getEventType(), event.getSagaId());

            sagaOrchestrator.handleSagaEvent(event);

        } catch (Exception e) {
            logger.error("Failed to process saga event: {}", eventJson, e);
        }
    }
}
