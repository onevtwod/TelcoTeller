package com.telco.userservice.saga.state;

public enum SagaStatus {
    PENDING, // Saga started but not yet completed
    IN_PROGRESS, // Saga is currently executing steps
    COMPLETED, // All steps completed successfully
    FAILED, // One or more steps failed
    COMPENSATED, // Saga was compensated due to failure
    TIMEOUT // Saga timed out
}
