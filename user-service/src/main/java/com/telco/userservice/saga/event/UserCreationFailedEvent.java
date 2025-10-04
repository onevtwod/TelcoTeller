package com.telco.userservice.saga.event;

public class UserCreationFailedEvent extends SagaEvent {
    private String errorMessage;
    private String errorCode;
    private String userId;

    public UserCreationFailedEvent() {
        super();
        setEventType("USER_CREATION_FAILED");
    }

    public UserCreationFailedEvent(String sagaId, String correlationId, String userId, String errorMessage,
            String errorCode) {
        super(sagaId, correlationId);
        setEventType("USER_CREATION_FAILED");
        this.userId = userId;
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
    }

    // Getters and Setters
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
