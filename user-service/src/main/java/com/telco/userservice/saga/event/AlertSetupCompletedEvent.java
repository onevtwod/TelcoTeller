package com.telco.userservice.saga.event;

public class AlertSetupCompletedEvent extends SagaEvent {
    private String userId;
    private String alertId;
    private double thresholdPercent;
    private boolean alertEnabled;

    public AlertSetupCompletedEvent() {
        super();
        setEventType("ALERT_SETUP_COMPLETED");
    }

    public AlertSetupCompletedEvent(String sagaId, String correlationId, String userId, String alertId,
            double thresholdPercent, boolean alertEnabled) {
        super(sagaId, correlationId);
        setEventType("ALERT_SETUP_COMPLETED");
        this.userId = userId;
        this.alertId = alertId;
        this.thresholdPercent = thresholdPercent;
        this.alertEnabled = alertEnabled;
    }

    // Getters and Setters
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
}
