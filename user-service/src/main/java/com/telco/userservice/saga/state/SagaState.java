package com.telco.userservice.saga.state;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SagaState {
    private String sagaId;
    private String correlationId;
    private SagaStatus status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<SagaStep> steps;
    private String currentStep;
    private String failureReason;
    private boolean compensationRequired;

    public SagaState() {
        this.steps = new ArrayList<>();
        this.status = SagaStatus.PENDING;
        this.startTime = LocalDateTime.now();
        this.compensationRequired = false;
    }

    public SagaState(String sagaId, String correlationId) {
        this();
        this.sagaId = sagaId;
        this.correlationId = correlationId;
    }

    public void addStep(SagaStep step) {
        this.steps.add(step);
        this.currentStep = step.getStepName();
    }

    public void markStepCompleted(String stepName) {
        steps.stream()
                .filter(step -> step.getStepName().equals(stepName))
                .findFirst()
                .ifPresent(step -> step.setStatus(SagaStepStatus.COMPLETED));
    }

    public void markStepFailed(String stepName, String errorMessage) {
        steps.stream()
                .filter(step -> step.getStepName().equals(stepName))
                .findFirst()
                .ifPresent(step -> {
                    step.setStatus(SagaStepStatus.FAILED);
                    step.setErrorMessage(errorMessage);
                });
        this.status = SagaStatus.FAILED;
        this.failureReason = errorMessage;
        this.compensationRequired = true;
    }

    public void markSagaCompleted() {
        this.status = SagaStatus.COMPLETED;
        this.endTime = LocalDateTime.now();
    }

    public void markSagaCompensated() {
        this.status = SagaStatus.COMPENSATED;
        this.endTime = LocalDateTime.now();
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

    public SagaStatus getStatus() {
        return status;
    }

    public void setStatus(SagaStatus status) {
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

    public List<SagaStep> getSteps() {
        return steps;
    }

    public void setSteps(List<SagaStep> steps) {
        this.steps = steps;
    }

    public String getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(String currentStep) {
        this.currentStep = currentStep;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public boolean isCompensationRequired() {
        return compensationRequired;
    }

    public void setCompensationRequired(boolean compensationRequired) {
        this.compensationRequired = compensationRequired;
    }
}
