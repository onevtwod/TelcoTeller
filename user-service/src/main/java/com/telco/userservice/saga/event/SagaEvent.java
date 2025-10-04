package com.telco.userservice.saga.event;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.time.LocalDateTime;
import java.util.UUID;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "eventType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = UserCreatedEvent.class, name = "USER_CREATED"),
        @JsonSubTypes.Type(value = UserCreationFailedEvent.class, name = "USER_CREATION_FAILED"),
        @JsonSubTypes.Type(value = AlertSetupCompletedEvent.class, name = "ALERT_SETUP_COMPLETED"),
        @JsonSubTypes.Type(value = AlertSetupFailedEvent.class, name = "ALERT_SETUP_FAILED"),
        @JsonSubTypes.Type(value = NotificationSentEvent.class, name = "NOTIFICATION_SENT"),
        @JsonSubTypes.Type(value = NotificationFailedEvent.class, name = "NOTIFICATION_FAILED"),
        @JsonSubTypes.Type(value = SagaCompensationEvent.class, name = "SAGA_COMPENSATION")
})
public abstract class SagaEvent {
    private String sagaId;
    private String eventId;
    private LocalDateTime timestamp;
    private String eventType;
    private String correlationId;

    public SagaEvent() {
        this.eventId = UUID.randomUUID().toString();
        this.timestamp = LocalDateTime.now();
    }

    public SagaEvent(String sagaId, String correlationId) {
        this();
        this.sagaId = sagaId;
        this.correlationId = correlationId;
    }

    // Getters and Setters
    public String getSagaId() {
        return sagaId;
    }

    public void setSagaId(String sagaId) {
        this.sagaId = sagaId;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }
}
