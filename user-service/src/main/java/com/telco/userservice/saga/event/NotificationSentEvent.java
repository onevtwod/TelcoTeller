package com.telco.userservice.saga.event;

public class NotificationSentEvent extends SagaEvent {
    private String userId;
    private String notificationId;
    private String notificationType;
    private String recipient;
    private boolean success;

    public NotificationSentEvent() {
        super();
        setEventType("NOTIFICATION_SENT");
    }

    public NotificationSentEvent(String sagaId, String correlationId, String userId, String notificationId,
            String notificationType, String recipient, boolean success) {
        super(sagaId, correlationId);
        setEventType("NOTIFICATION_SENT");
        this.userId = userId;
        this.notificationId = notificationId;
        this.notificationType = notificationType;
        this.recipient = recipient;
        this.success = success;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
