package com.telco.userservice.saga.event;

public class NotificationFailedEvent extends SagaEvent {
    private String userId;
    private String errorMessage;
    private String errorCode;
    private String notificationType;

    public NotificationFailedEvent() {
        super();
        setEventType("NOTIFICATION_FAILED");
    }

    public NotificationFailedEvent(String sagaId, String correlationId, String userId, String notificationType,
            String errorMessage, String errorCode) {
        super(sagaId, correlationId);
        setEventType("NOTIFICATION_FAILED");
        this.userId = userId;
        this.notificationType = notificationType;
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }
}
