package com.telco.userservice.saga.retry;

import com.telco.userservice.saga.event.SagaCompensationEvent;
import com.telco.userservice.saga.event.SagaEvent;
import com.telco.userservice.saga.persistence.SagaStateRepository;
import com.telco.userservice.saga.producer.SagaEventProducer;
import com.telco.userservice.saga.state.SagaState;
import com.telco.userservice.saga.state.SagaStep;
import com.telco.userservice.saga.state.SagaStepStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Component
public class SagaRetryManager {

    private static final Logger logger = LoggerFactory.getLogger(SagaRetryManager.class);
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final Duration RETRY_DELAY = Duration.ofSeconds(5);

    @Autowired
    private SagaStateRepository sagaStateRepository;

    @Autowired
    private SagaEventProducer sagaEventProducer;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    @Retryable(value = {
            Exception.class }, maxAttempts = MAX_RETRY_ATTEMPTS, backoff = @Backoff(delay = 5000, multiplier = 2))
    public CompletableFuture<Void> retrySagaStep(String sagaId, String stepName, Runnable stepAction) {
        logger.info("Retrying saga step: {} for saga: {}", stepName, sagaId);

        try {
            stepAction.run();
            markStepCompleted(sagaId, stepName);
            return CompletableFuture.completedFuture(null);

        } catch (Exception e) {
            logger.warn("Saga step failed, will retry: {} for saga: {} - {}", stepName, sagaId, e.getMessage());
            throw e;
        }
    }

    @Recover
    public CompletableFuture<Void> recoverSagaStep(Exception ex, String sagaId, String stepName, Runnable stepAction) {
        logger.error("Saga step failed after all retries: {} for saga: {} - {}", stepName, sagaId, ex.getMessage());

        // Mark step as failed
        markStepFailed(sagaId, stepName, ex.getMessage());

        // Trigger compensation
        triggerCompensation(sagaId, "Step failed after retries: " + ex.getMessage());

        return CompletableFuture.completedFuture(null);
    }

    public void scheduleRetry(String sagaId, String stepName, Runnable stepAction, int attemptNumber) {
        if (attemptNumber >= MAX_RETRY_ATTEMPTS) {
            logger.error("Max retry attempts reached for saga step: {} for saga: {}", stepName, sagaId);
            markStepFailed(sagaId, stepName, "Max retry attempts reached");
            triggerCompensation(sagaId, "Max retry attempts reached");
            return;
        }

        // Calculate delay with exponential backoff
        long delay = RETRY_DELAY.toMillis() * (long) Math.pow(2, attemptNumber);

        logger.info("Scheduling retry {} for saga step: {} for saga: {} in {}ms",
                attemptNumber + 1, stepName, sagaId, delay);

        scheduler.schedule(() -> {
            try {
                stepAction.run();
                markStepCompleted(sagaId, stepName);
            } catch (Exception e) {
                scheduleRetry(sagaId, stepName, stepAction, attemptNumber + 1);
            }
        }, delay, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    public boolean shouldRetry(String sagaId, String stepName) {
        SagaState sagaState = sagaStateRepository.getSagaState(sagaId).orElse(null);
        if (sagaState == null) {
            return false;
        }

        SagaStep step = sagaState.getSteps().stream()
                .filter(s -> s.getStepName().equals(stepName))
                .findFirst()
                .orElse(null);

        if (step == null) {
            return false;
        }

        // Check if step has failed and can be retried
        return step.getStatus() == SagaStepStatus.FAILED &&
                getRetryCount(step) < MAX_RETRY_ATTEMPTS;
    }

    private int getRetryCount(SagaStep step) {
        // In a real implementation, you would store retry count in the step
        // For now, we'll use a simple approach based on timestamps
        if (step.getEndTime() == null) {
            return 0;
        }

        Duration timeSinceFailure = Duration.between(step.getEndTime(), LocalDateTime.now());
        return (int) (timeSinceFailure.toMinutes() / RETRY_DELAY.toMinutes());
    }

    private void markStepCompleted(String sagaId, String stepName) {
        SagaState sagaState = sagaStateRepository.getSagaState(sagaId).orElse(null);
        if (sagaState != null) {
            sagaState.getSteps().stream()
                    .filter(step -> step.getStepName().equals(stepName))
                    .findFirst()
                    .ifPresent(step -> {
                        step.setStatus(SagaStepStatus.COMPLETED);
                        step.setEndTime(LocalDateTime.now());
                    });
            sagaStateRepository.saveSagaState(sagaState);
        }
    }

    private void markStepFailed(String sagaId, String stepName, String errorMessage) {
        SagaState sagaState = sagaStateRepository.getSagaState(sagaId).orElse(null);
        if (sagaState != null) {
            sagaState.getSteps().stream()
                    .filter(step -> step.getStepName().equals(stepName))
                    .findFirst()
                    .ifPresent(step -> {
                        step.setStatus(SagaStepStatus.FAILED);
                        step.setErrorMessage(errorMessage);
                        step.setEndTime(LocalDateTime.now());
                    });
            sagaState.setCompensationRequired(true);
            sagaStateRepository.saveSagaState(sagaState);
        }
    }

    private void triggerCompensation(String sagaId, String reason) {
        SagaState sagaState = sagaStateRepository.getSagaState(sagaId).orElse(null);
        if (sagaState != null) {
            SagaCompensationEvent compensationEvent = new SagaCompensationEvent(
                    sagaId,
                    sagaState.getCorrelationId(),
                    reason,
                    sagaState.getCurrentStep(),
                    new HashMap<String, Object>() {
                        {
                            put("retryExhausted", "true");
                            put("reason", reason);
                        }
                    });

            sagaEventProducer.publishSagaEvent(compensationEvent);
        }
    }

    public void handleRetryableFailure(String sagaId, String stepName, Exception exception) {
        logger.warn("Handling retryable failure for saga step: {} for saga: {} - {}",
                stepName, sagaId, exception.getMessage());

        if (shouldRetry(sagaId, stepName)) {
            scheduleRetry(sagaId, stepName, () -> {
                // This would be the actual step action
                throw new RuntimeException("Retry action not implemented");
            }, 0);
        } else {
            markStepFailed(sagaId, stepName, exception.getMessage());
            triggerCompensation(sagaId, "Step failed and retries exhausted");
        }
    }
}
