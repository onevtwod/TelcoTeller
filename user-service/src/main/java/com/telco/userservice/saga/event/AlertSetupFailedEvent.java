package com.telco.userservice.saga.event;

public class AlertSetupFailedEvent extends SagaEvent {
    private String userId;
    private String errorMessage;
    private String errorCode;

    public AlertSetupFailedEvent() {
        super();
        setEventType("ALERT_SETUP_FAILED");
    }

    public AlertSetupFailedEvent(String sagaId, String correlationId, String userId, String errorMessage,
            String errorCode) {
        super(sagaId, correlationId);
        setEventType("ALERT_SETUP_FAILED");
        this.userId = userId;
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
}
