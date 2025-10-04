package com.telco.userservice.saga.state;

import java.time.LocalDateTime;

public class SagaStep {
    private String stepName;
    private String serviceName;
    private SagaStepStatus status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String errorMessage;
    private String compensationAction;
    private Object stepData;

    public SagaStep() {
        this.status = SagaStepStatus.PENDING;
        this.startTime = LocalDateTime.now();
    }

    public SagaStep(String stepName, String serviceName) {
        this();
        this.stepName = stepName;
        this.serviceName = serviceName;
    }

    public SagaStep(String stepName, String serviceName, String compensationAction) {
        this(stepName, serviceName);
        this.compensationAction = compensationAction;
    }

    // Getters and Setters
    public String getStepName() {
        return stepName;
    }

    public void setStepName(String stepName) {
        this.stepName = stepName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public SagaStepStatus getStatus() {
        return status;
    }

    public void setStatus(SagaStepStatus status) {
        this.status = status;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getCompensationAction() {
        return compensationAction;
    }

    public void setCompensationAction(String compensationAction) {
        this.compensationAction = compensationAction;
    }

    public Object getStepData() {
        return stepData;
    }

    public void setStepData(Object stepData) {
        this.stepData = stepData;
    }
}
