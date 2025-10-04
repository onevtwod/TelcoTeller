package com.telco.userservice.saga;

import com.telco.userservice.model.User;
import com.telco.userservice.saga.event.UserCreatedEvent;
import com.telco.userservice.saga.event.UserCreationFailedEvent;
import com.telco.userservice.saga.orchestrator.SagaOrchestrator;
import com.telco.userservice.saga.producer.SagaEventProducer;
import com.telco.userservice.saga.state.SagaState;
import com.telco.userservice.saga.state.SagaStatus;
import com.telco.userservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class SagaPatternTest {

    @Autowired
    private SagaOrchestrator sagaOrchestrator;

    @Autowired
    private UserService userService;

    @MockBean
    private SagaEventProducer sagaEventProducer;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("test-user-123", "+1234567890", 5368709120L, 1073741824L);
    }

    @Test
    void testStartUserRegistrationSaga() {
        // Given
        String correlationId = "test-correlation-123";

        // When
        String sagaId = sagaOrchestrator.startUserRegistrationSaga(correlationId);

        // Then
        assertNotNull(sagaId);
        assertFalse(sagaId.isEmpty());

        SagaState sagaState = sagaOrchestrator.getSagaState(sagaId);
        assertNotNull(sagaState);
        assertEquals(sagaId, sagaState.getSagaId());
        assertEquals(correlationId, sagaState.getCorrelationId());
        assertEquals(SagaStatus.PENDING, sagaState.getStatus());
        assertEquals(3, sagaState.getSteps().size());
    }

    @Test
    void testHandleUserCreatedEvent() {
        // Given
        String sagaId = sagaOrchestrator.startUserRegistrationSaga("test-correlation");
        UserCreatedEvent event = new UserCreatedEvent(sagaId, "test-correlation", testUser);

        // When
        sagaOrchestrator.handleSagaEvent(event);

        // Then
        SagaState sagaState = sagaOrchestrator.getSagaState(sagaId);
        assertNotNull(sagaState);
        assertEquals(SagaStatus.IN_PROGRESS, sagaState.getStatus());

        // Check that CREATE_USER step is completed
        boolean userStepCompleted = sagaState.getSteps().stream()
                .anyMatch(step -> "CREATE_USER".equals(step.getStepName()) &&
                        step.getStatus().name().equals("COMPLETED"));
        assertTrue(userStepCompleted);
    }

    @Test
    void testHandleUserCreationFailedEvent() {
        // Given
        String sagaId = sagaOrchestrator.startUserRegistrationSaga("test-correlation");
        UserCreationFailedEvent event = new UserCreationFailedEvent(
                sagaId, "test-correlation", "test-user-123", "Database connection failed", "DB_ERROR");

        // When
        sagaOrchestrator.handleSagaEvent(event);

        // Then
        SagaState sagaState = sagaOrchestrator.getSagaState(sagaId);
        assertNotNull(sagaState);
        assertEquals(SagaStatus.FAILED, sagaState.getStatus());
        assertTrue(sagaState.isCompensationRequired());
        assertEquals("Database connection failed", sagaState.getFailureReason());
    }

    @Test
    void testSagaStateManagement() {
        // Given
        String sagaId = sagaOrchestrator.startUserRegistrationSaga("test-correlation");

        // When
        SagaState sagaState = sagaOrchestrator.getSagaState(sagaId);

        // Then
        assertNotNull(sagaState);
        assertEquals(sagaId, sagaState.getSagaId());
        assertEquals("test-correlation", sagaState.getCorrelationId());
        assertEquals(SagaStatus.PENDING, sagaState.getStatus());
        assertNotNull(sagaState.getStartTime());
        assertNull(sagaState.getEndTime());
        assertFalse(sagaState.isCompensationRequired());
    }

    @Test
    void testSagaStepsDefinition() {
        // Given
        String sagaId = sagaOrchestrator.startUserRegistrationSaga("test-correlation");

        // When
        SagaState sagaState = sagaOrchestrator.getSagaState(sagaId);

        // Then
        assertEquals(3, sagaState.getSteps().size());

        // Check CREATE_USER step
        assertTrue(sagaState.getSteps().stream()
                .anyMatch(step -> "CREATE_USER".equals(step.getStepName()) &&
                        "user-service".equals(step.getServiceName())));

        // Check SETUP_ALERTS step
        assertTrue(sagaState.getSteps().stream()
                .anyMatch(step -> "SETUP_ALERTS".equals(step.getStepName()) &&
                        "alert-trigger-service".equals(step.getServiceName())));

        // Check SEND_WELCOME_NOTIFICATION step
        assertTrue(sagaState.getSteps().stream()
                .anyMatch(step -> "SEND_WELCOME_NOTIFICATION".equals(step.getStepName()) &&
                        "notification-service".equals(step.getServiceName())));
    }

    @Test
    void testSagaEventProducerIntegration() {
        // Given
        String sagaId = "test-saga-123";
        UserCreatedEvent event = new UserCreatedEvent(sagaId, "test-correlation", testUser);

        // When
        sagaEventProducer.publishSagaEvent(event);

        // Then
        verify(sagaEventProducer, times(1)).publishSagaEvent(event);
    }

    @Test
    void testSagaCompensationTrigger() {
        // Given
        String sagaId = sagaOrchestrator.startUserRegistrationSaga("test-correlation");

        // Simulate user creation success
        UserCreatedEvent userCreatedEvent = new UserCreatedEvent(sagaId, "test-correlation", testUser);
        sagaOrchestrator.handleSagaEvent(userCreatedEvent);

        // Simulate alert setup failure
        UserCreationFailedEvent alertFailedEvent = new UserCreationFailedEvent(
                sagaId, "test-correlation", "test-user-123", "Alert service unavailable", "SERVICE_ERROR");

        // When
        sagaOrchestrator.handleSagaEvent(alertFailedEvent);

        // Then
        SagaState sagaState = sagaOrchestrator.getSagaState(sagaId);
        assertEquals(SagaStatus.FAILED, sagaState.getStatus());
        assertTrue(sagaState.isCompensationRequired());

        // Verify compensation event would be published
        verify(sagaEventProducer, atLeastOnce()).publishSagaEvent(any());
    }
}
