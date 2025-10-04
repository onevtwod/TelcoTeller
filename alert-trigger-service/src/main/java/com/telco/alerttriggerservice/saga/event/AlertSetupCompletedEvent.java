package com.telco.alerttriggerservice.saga.event;

public class AlertSetupCompletedEvent {
    private String sagaId;
    private String correlationId;
    private String userId;
    private String alertId;
    private double thresholdPercent;
    private boolean alertEnabled;
    private String eventType = "ALERT_SETUP_COMPLETED";

    public AlertSetupCompletedEvent() {
    }

    public AlertSetupCompletedEvent(String sagaId, String correlationId, String userId,
            String alertId, double thresholdPercent, boolean alertEnabled) {
        this.sagaId = sagaId;
        this.correlationId = correlationId;
        this.userId = userId;
        this.alertId = alertId;
        this.thresholdPercent = thresholdPercent;
        this.alertEnabled = alertEnabled;
    }

    // Getters and Setters
    public String getSagaId() {
        return sagaId;
    }

    public void setSagaId(String sagaId) {
        this.sagaId = sagaId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAlertId() {
        return alertId;
    }

    public void setAlertId(String alertId) {
        this.alertId = alertId;
    }

    public double getThresholdPercent() {
        return thresholdPercent;
    }

    public void setThresholdPercent(double thresholdPercent) {
        this.thresholdPercent = thresholdPercent;
    }

    public boolean isAlertEnabled() {
        return alertEnabled;
    }

    public void setAlertEnabled(boolean alertEnabled) {
        this.alertEnabled = alertEnabled;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
}
