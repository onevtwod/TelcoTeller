package com.telco.userservice.saga.timeout;

import com.telco.userservice.saga.event.SagaCompensationEvent;
import com.telco.userservice.saga.orchestrator.SagaOrchestrator;
import com.telco.userservice.saga.persistence.SagaStateRepository;
import com.telco.userservice.saga.producer.SagaEventProducer;
import com.telco.userservice.saga.state.SagaState;
import com.telco.userservice.saga.state.SagaStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Component
public class SagaTimeoutManager {

    private static final Logger logger = LoggerFactory.getLogger(SagaTimeoutManager.class);
    private static final Duration SAGA_TIMEOUT = Duration.ofMinutes(10); // 10 minutes timeout
    private static final Duration STEP_TIMEOUT = Duration.ofMinutes(2); // 2 minutes per step

    @Autowired
    private SagaStateRepository sagaStateRepository;

    @Autowired
    private SagaEventProducer sagaEventProducer;

    @Autowired
    private SagaOrchestrator sagaOrchestrator;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    @Scheduled(fixedDelay = 30000) // Check every 30 seconds
    public void checkSagaTimeouts() {
        logger.debug("Checking for saga timeouts...");

        // In production, you would query Redis for all active sagas
        // For now, we'll check in-memory sagas
        List<String> activeSagaIds = sagaOrchestrator.getActiveSagaIds();

        for (String sagaId : activeSagaIds) {
            checkSagaTimeout(sagaId);
        }
    }

    @Async
    public CompletableFuture<Void> checkSagaTimeout(String sagaId) {
        try {
            SagaState sagaState = sagaStateRepository.getSagaState(sagaId).orElse(null);
            if (sagaState == null) {
                return CompletableFuture.completedFuture(null);
            }

            // Check overall saga timeout
            if (isSagaTimedOut(sagaState)) {
                handleSagaTimeout(sagaState, "Saga overall timeout exceeded");
                return CompletableFuture.completedFuture(null);
            }

            // Check step timeout
            if (isStepTimedOut(sagaState)) {
                handleStepTimeout(sagaState, "Saga step timeout exceeded");
                return CompletableFuture.completedFuture(null);
            }

        } catch (Exception e) {
            logger.error("Error checking timeout for saga: {}", sagaId, e);
        }

        return CompletableFuture.completedFuture(null);
    }

    private boolean isSagaTimedOut(SagaState sagaState) {
        if (sagaState.getStatus() != SagaStatus.IN_PROGRESS) {
            return false;
        }

        Duration elapsed = Duration.between(sagaState.getStartTime(), LocalDateTime.now());
        return elapsed.compareTo(SAGA_TIMEOUT) > 0;
    }

    private boolean isStepTimedOut(SagaState sagaState) {
        if (sagaState.getStatus() != SagaStatus.IN_PROGRESS) {
            return false;
        }

        // Check if current step has been running too long
        return sagaState.getSteps().stream()
                .anyMatch(step -> step.getStatus().name().equals("IN_PROGRESS") &&
                        Duration.between(step.getStartTime(), LocalDateTime.now()).compareTo(STEP_TIMEOUT) > 0);
    }

    private void handleSagaTimeout(SagaState sagaState, String reason) {
        logger.warn("Saga timeout detected for saga: {} - {}", sagaState.getSagaId(), reason);

        // Mark saga as timed out
        sagaState.setStatus(SagaStatus.TIMEOUT);
        sagaState.setFailureReason(reason);
        sagaState.setCompensationRequired(true);

        // Save updated state
        sagaStateRepository.saveSagaState(sagaState);

        // Trigger compensation
        SagaCompensationEvent compensationEvent = new SagaCompensationEvent(
                sagaState.getSagaId(),
                sagaState.getCorrelationId(),
                reason,
                sagaState.getCurrentStep(),
                new HashMap<String, Object>() {
                    {
                        put("timeout", true);
                        put("reason", reason);
                    }
                });

        sagaEventProducer.publishSagaEvent(compensationEvent);

        logger.info("Saga timeout compensation triggered for saga: {}", sagaState.getSagaId());
    }

    private void handleStepTimeout(SagaState sagaState, String reason) {
        logger.warn("Step timeout detected for saga: {} - {}", sagaState.getSagaId(), reason);

        // Mark current step as failed
        sagaState.getSteps().stream()
                .filter(step -> step.getStatus().name().equals("IN_PROGRESS"))
                .findFirst()
                .ifPresent(step -> {
                    step.setStatus(com.telco.userservice.saga.state.SagaStepStatus.FAILED);
                    step.setErrorMessage(reason);
                });

        // Mark saga as failed
        sagaState.setStatus(SagaStatus.FAILED);
        sagaState.setFailureReason(reason);
        sagaState.setCompensationRequired(true);

        // Save updated state
        sagaStateRepository.saveSagaState(sagaState);

        // Trigger compensation
        SagaCompensationEvent compensationEvent = new SagaCompensationEvent(
                sagaState.getSagaId(),
                sagaState.getCorrelationId(),
                reason,
                sagaState.getCurrentStep(),
                new HashMap<String, Object>() {
                    {
                        put("timeout", true);
                        put("reason", reason);
                    }
                });

        sagaEventProducer.publishSagaEvent(compensationEvent);

        logger.info("Step timeout compensation triggered for saga: {}", sagaState.getSagaId());
    }

    public void scheduleSagaTimeout(String sagaId) {
        // Schedule a timeout check for this specific saga
        scheduler.schedule(() -> checkSagaTimeout(sagaId),
                SAGA_TIMEOUT.toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS);
    }
}
