package com.telco.userservice.saga.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class SagaMetrics {

    private final MeterRegistry meterRegistry;
    private final ConcurrentHashMap<String, LocalDateTime> sagaStartTimes = new ConcurrentHashMap<>();
    private final AtomicLong activeSagas = new AtomicLong(0);
    private final AtomicLong completedSagas = new AtomicLong(0);
    private final AtomicLong failedSagas = new AtomicLong(0);
    private final AtomicLong compensatedSagas = new AtomicLong(0);
    private final AtomicLong timedOutSagas = new AtomicLong(0);

    // Counters
    private final Counter sagaStartedCounter;
    private final Counter sagaCompletedCounter;
    private final Counter sagaFailedCounter;
    private final Counter sagaCompensatedCounter;
    private final Counter sagaTimedOutCounter;
    private final Counter sagaStepRetryCounter;
    private final Counter sagaCompensationTriggeredCounter;

    // Timers
    private final Timer sagaDurationTimer;
    private final Timer sagaStepDurationTimer;

    @Autowired
    public SagaMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        // Initialize counters
        this.sagaStartedCounter = Counter.builder("saga.started.total")
                .description("Total number of sagas started")
                .register(meterRegistry);

        this.sagaCompletedCounter = Counter.builder("saga.completed.total")
                .description("Total number of sagas completed successfully")
                .register(meterRegistry);

        this.sagaFailedCounter = Counter.builder("saga.failed.total")
                .description("Total number of sagas that failed")
                .register(meterRegistry);

        this.sagaCompensatedCounter = Counter.builder("saga.compensated.total")
                .description("Total number of sagas that were compensated")
                .register(meterRegistry);

        this.sagaTimedOutCounter = Counter.builder("saga.timed_out.total")
                .description("Total number of sagas that timed out")
                .register(meterRegistry);

        this.sagaStepRetryCounter = Counter.builder("saga.step.retry.total")
                .description("Total number of saga step retries")
                .register(meterRegistry);

        this.sagaCompensationTriggeredCounter = Counter.builder("saga.compensation.triggered.total")
                .description("Total number of compensation events triggered")
                .register(meterRegistry);

        // Initialize timers
        this.sagaDurationTimer = Timer.builder("saga.duration")
                .description("Saga execution duration")
                .register(meterRegistry);

        this.sagaStepDurationTimer = Timer.builder("saga.step.duration")
                .description("Saga step execution duration")
                .register(meterRegistry);

        // Initialize gauges
        Gauge.builder("saga.active.count", this, SagaMetrics::getActiveSagaCount)
                .description("Number of active sagas")
                .register(meterRegistry);

        Gauge.builder("saga.completed.count", this, SagaMetrics::getCompletedSagaCount)
                .description("Number of completed sagas")
                .register(meterRegistry);

        Gauge.builder("saga.failed.count", this, SagaMetrics::getFailedSagaCount)
                .description("Number of failed sagas")
                .register(meterRegistry);

        Gauge.builder("saga.compensated.count", this, SagaMetrics::getCompensatedSagaCount)
                .description("Number of compensated sagas")
                .register(meterRegistry);

        Gauge.builder("saga.timed_out.count", this, SagaMetrics::getTimedOutSagaCount)
                .description("Number of timed out sagas")
                .register(meterRegistry);
    }

    public void recordSagaStarted(String sagaId) {
        sagaStartedCounter.increment();
        activeSagas.incrementAndGet();
        sagaStartTimes.put(sagaId, LocalDateTime.now());
    }

    public void recordSagaCompleted(String sagaId) {
        sagaCompletedCounter.increment();
        completedSagas.incrementAndGet();
        activeSagas.decrementAndGet();

        LocalDateTime startTime = sagaStartTimes.remove(sagaId);
        if (startTime != null) {
            Duration duration = Duration.between(startTime, LocalDateTime.now());
            sagaDurationTimer.record(duration);
        }
    }

    public void recordSagaFailed(String sagaId) {
        sagaFailedCounter.increment();
        failedSagas.incrementAndGet();
        activeSagas.decrementAndGet();

        LocalDateTime startTime = sagaStartTimes.remove(sagaId);
        if (startTime != null) {
            Duration duration = Duration.between(startTime, LocalDateTime.now());
            sagaDurationTimer.record(duration);
        }
    }

    public void recordSagaCompensated(String sagaId) {
        sagaCompensatedCounter.increment();
        compensatedSagas.incrementAndGet();
        activeSagas.decrementAndGet();

        LocalDateTime startTime = sagaStartTimes.remove(sagaId);
        if (startTime != null) {
            Duration duration = Duration.between(startTime, LocalDateTime.now());
            sagaDurationTimer.record(duration);
        }
    }

    public void recordSagaTimedOut(String sagaId) {
        sagaTimedOutCounter.increment();
        timedOutSagas.incrementAndGet();
        activeSagas.decrementAndGet();

        LocalDateTime startTime = sagaStartTimes.remove(sagaId);
        if (startTime != null) {
            Duration duration = Duration.between(startTime, LocalDateTime.now());
            sagaDurationTimer.record(duration);
        }
    }

    public void recordSagaStepRetry(String stepName) {
        sagaStepRetryCounter.increment();
    }

    public void recordSagaCompensationTriggered(String reason) {
        sagaCompensationTriggeredCounter.increment();
    }

    public void recordSagaStepDuration(String stepName, Duration duration) {
        sagaStepDurationTimer.record(duration.toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    // Gauge methods
    public double getActiveSagaCount() {
        return activeSagas.get();
    }

    public double getCompletedSagaCount() {
        return completedSagas.get();
    }

    public double getFailedSagaCount() {
        return failedSagas.get();
    }

    public double getCompensatedSagaCount() {
        return compensatedSagas.get();
    }

    public double getTimedOutSagaCount() {
        return timedOutSagas.get();
    }

    public double getSagaSuccessRate() {
        long total = completedSagas.get() + failedSagas.get() + compensatedSagas.get() + timedOutSagas.get();
        if (total == 0)
            return 0.0;
        return (double) completedSagas.get() / total * 100.0;
    }

    public double getSagaFailureRate() {
        long total = completedSagas.get() + failedSagas.get() + compensatedSagas.get() + timedOutSagas.get();
        if (total == 0)
            return 0.0;
        return (double) (failedSagas.get() + timedOutSagas.get()) / total * 100.0;
    }

    public double getSagaCompensationRate() {
        long total = completedSagas.get() + failedSagas.get() + compensatedSagas.get() + timedOutSagas.get();
        if (total == 0)
            return 0.0;
        return (double) compensatedSagas.get() / total * 100.0;
    }
}
