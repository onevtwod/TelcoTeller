package com.telco.userservice.saga.event;

import java.util.Map;

public class SagaCompensationEvent extends SagaEvent {
    private String compensationReason;
    private String failedStep;
    private Map<String, Object> compensationData;

    public SagaCompensationEvent() {
        super();
        setEventType("SAGA_COMPENSATION");
    }

    public SagaCompensationEvent(String sagaId, String correlationId, String compensationReason,
            String failedStep, Map<String, Object> compensationData) {
        super(sagaId, correlationId);
        setEventType("SAGA_COMPENSATION");
        this.compensationReason = compensationReason;
        this.failedStep = failedStep;
        this.compensationData = compensationData;
    }

    // Getters and Setters
    public String getCompensationReason() {
        return compensationReason;
    }

    public void setCompensationReason(String compensationReason) {
        this.compensationReason = compensationReason;
    }

    public String getFailedStep() {
        return failedStep;
    }

    public void setFailedStep(String failedStep) {
        this.failedStep = failedStep;
    }

    public Map<String, Object> getCompensationData() {
        return compensationData;
    }

    public void setCompensationData(Map<String, Object> compensationData) {
        this.compensationData = compensationData;
    }
}
