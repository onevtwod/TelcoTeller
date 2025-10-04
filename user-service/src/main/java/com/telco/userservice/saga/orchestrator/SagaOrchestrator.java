package com.telco.userservice.saga.orchestrator;

import com.telco.userservice.saga.event.*;
import com.telco.userservice.saga.producer.SagaEventProducer;
import com.telco.userservice.saga.state.SagaState;
import com.telco.userservice.saga.state.SagaStatus;
import com.telco.userservice.saga.state.SagaStep;
import com.telco.userservice.saga.state.SagaStepStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Collections;

@Component
public class SagaOrchestrator {

    private static final Logger logger = LoggerFactory.getLogger(SagaOrchestrator.class);

    @Autowired
    private SagaEventProducer sagaEventProducer;

    // In-memory saga state storage (in production, use Redis or database)
    private final Map<String, SagaState> sagaStates = new ConcurrentHashMap<>();

    public java.util.List<String> getActiveSagaIds() {
        return sagaStates.values().stream()
                .filter(state -> state.getStatus() == SagaStatus.IN_PROGRESS)
                .map(SagaState::getSagaId)
                .collect(java.util.stream.Collectors.toList());
    }

    public String startUserRegistrationSaga(String correlationId) {
        String sagaId = UUID.randomUUID().toString();
        SagaState sagaState = new SagaState(sagaId, correlationId);

        // Define saga steps
        sagaState.addStep(new SagaStep("CREATE_USER", "user-service", "DELETE_USER"));
        sagaState.addStep(new SagaStep("SETUP_ALERTS", "alert-trigger-service", "DISABLE_ALERTS"));
        sagaState.addStep(
                new SagaStep("SEND_WELCOME_NOTIFICATION", "notification-service", "SEND_FAILURE_NOTIFICATION"));

        sagaStates.put(sagaId, sagaState);

        logger.info("Started user registration saga: {} with correlation: {}", sagaId, correlationId);

        // Start the first step
        startNextStep(sagaId);

        return sagaId;
    }

    public void handleSagaEvent(SagaEvent event) {
        String sagaId = event.getSagaId();
        SagaState sagaState = sagaStates.get(sagaId);

        if (sagaState == null) {
            logger.warn("Received event for unknown saga: {}", sagaId);
            return;
        }

        logger.info("Processing saga event: {} for saga: {}", event.getEventType(), sagaId);

        switch (event.getEventType()) {
            case "USER_CREATED":
                handleUserCreated((UserCreatedEvent) event, sagaState);
                break;
            case "USER_CREATION_FAILED":
                handleUserCreationFailed((UserCreationFailedEvent) event, sagaState);
                break;
            case "ALERT_SETUP_COMPLETED":
                handleAlertSetupCompleted((AlertSetupCompletedEvent) event, sagaState);
                break;
            case "ALERT_SETUP_FAILED":
                handleAlertSetupFailed((AlertSetupFailedEvent) event, sagaState);
                break;
            case "NOTIFICATION_SENT":
                handleNotificationSent((NotificationSentEvent) event, sagaState);
                break;
            case "NOTIFICATION_FAILED":
                handleNotificationFailed((NotificationFailedEvent) event, sagaState);
                break;
            case "SAGA_COMPENSATION":
                handleSagaCompensation((SagaCompensationEvent) event, sagaState);
                break;
            default:
                logger.warn("Unknown saga event type: {}", event.getEventType());
        }
    }

    private void handleUserCreated(UserCreatedEvent event, SagaState sagaState) {
        sagaState.markStepCompleted("CREATE_USER");
        logger.info("User created successfully for saga: {}", event.getSagaId());
        startNextStep(event.getSagaId());
    }

    private void handleUserCreationFailed(UserCreationFailedEvent event, SagaState sagaState) {
        sagaState.markStepFailed("CREATE_USER", event.getErrorMessage());
        logger.error("User creation failed for saga: {} - {}", event.getSagaId(), event.getErrorMessage());
        triggerCompensation(event.getSagaId(), "User creation failed");
    }

    private void handleAlertSetupCompleted(AlertSetupCompletedEvent event, SagaState sagaState) {
        sagaState.markStepCompleted("SETUP_ALERTS");
        logger.info("Alert setup completed for saga: {}", event.getSagaId());
        startNextStep(event.getSagaId());
    }

    private void handleAlertSetupFailed(AlertSetupFailedEvent event, SagaState sagaState) {
        sagaState.markStepFailed("SETUP_ALERTS", event.getErrorMessage());
        logger.error("Alert setup failed for saga: {} - {}", event.getSagaId(), event.getErrorMessage());
        triggerCompensation(event.getSagaId(), "Alert setup failed");
    }

    private void handleNotificationSent(NotificationSentEvent event, SagaState sagaState) {
        sagaState.markStepCompleted("SEND_WELCOME_NOTIFICATION");
        logger.info("Notification sent successfully for saga: {}", event.getSagaId());
        completeSaga(event.getSagaId());
    }

    private void handleNotificationFailed(NotificationFailedEvent event, SagaState sagaState) {
        sagaState.markStepFailed("SEND_WELCOME_NOTIFICATION", event.getErrorMessage());
        logger.error("Notification failed for saga: {} - {}", event.getSagaId(), event.getErrorMessage());
        triggerCompensation(event.getSagaId(), "Notification failed");
    }

    private void handleSagaCompensation(SagaCompensationEvent event, SagaState sagaState) {
        logger.info("Saga compensation completed for saga: {} - {}", event.getSagaId(), event.getCompensationReason());
        sagaState.markSagaCompensated();
    }

    private void startNextStep(String sagaId) {
        SagaState sagaState = sagaStates.get(sagaId);
        if (sagaState == null)
            return;

        // Find next pending step
        SagaStep nextStep = sagaState.getSteps().stream()
                .filter(step -> step.getStatus() == SagaStepStatus.PENDING)
                .findFirst()
                .orElse(null);

        if (nextStep == null) {
            completeSaga(sagaId);
            return;
        }

        nextStep.setStatus(SagaStepStatus.IN_PROGRESS);
        logger.info("Starting next step: {} for saga: {}", nextStep.getStepName(), sagaId);

        // Trigger the next step based on step name
        switch (nextStep.getStepName()) {
            case "CREATE_USER":
                // This step is already handled by the service that starts the saga
                break;
            case "SETUP_ALERTS":
                triggerAlertSetup(sagaId, sagaState);
                break;
            case "SEND_WELCOME_NOTIFICATION":
                triggerWelcomeNotification(sagaId, sagaState);
                break;
        }
    }

    private void triggerAlertSetup(String sagaId, SagaState sagaState) {
        // Find user data from the saga state
        // In a real implementation, you would store user data in the saga state
        logger.info("Triggering alert setup for saga: {}", sagaId);
        // This would typically send a message to the alert service
    }

    private void triggerWelcomeNotification(String sagaId, SagaState sagaState) {
        logger.info("Triggering welcome notification for saga: {}", sagaId);
        // This would typically send a message to the notification service
    }

    private void completeSaga(String sagaId) {
        SagaState sagaState = sagaStates.get(sagaId);
        if (sagaState != null) {
            sagaState.markSagaCompleted();
            logger.info("Saga completed successfully: {}", sagaId);
        }
    }

    private void triggerCompensation(String sagaId, String reason) {
        SagaState sagaState = sagaStates.get(sagaId);
        if (sagaState == null)
            return;

        logger.info("Triggering compensation for saga: {} - {}", sagaId, reason);

        // Find the last completed step and trigger its compensation
        SagaStep lastCompletedStep = sagaState.getSteps().stream()
                .filter(step -> step.getStatus() == SagaStepStatus.COMPLETED)
                .reduce((first, second) -> second)
                .orElse(null);

        if (lastCompletedStep != null) {
            SagaCompensationEvent compensationEvent = new SagaCompensationEvent(
                    sagaId, sagaState.getCorrelationId(), reason, lastCompletedStep.getStepName(),
                    Collections.singletonMap("compensationAction", lastCompletedStep.getCompensationAction()));
            sagaEventProducer.publishSagaEvent(compensationEvent);
        }
    }

    public SagaState getSagaState(String sagaId) {
        return sagaStates.get(sagaId);
    }
}
