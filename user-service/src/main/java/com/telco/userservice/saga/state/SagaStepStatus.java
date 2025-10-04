package com.telco.userservice.saga.state;

public enum SagaStepStatus {
    PENDING, // Step not yet started
    IN_PROGRESS, // Step currently executing
    COMPLETED, // Step completed successfully
    FAILED, // Step failed
    COMPENSATED, // Step was compensated
    SKIPPED // Step was skipped
}
