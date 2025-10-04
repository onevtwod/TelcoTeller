package com.telco.userservice.web;

import com.telco.userservice.model.User;
import com.telco.userservice.saga.orchestrator.SagaOrchestrator;
import com.telco.userservice.saga.state.SagaState;
import com.telco.userservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/saga")
public class SagaController {

    @Autowired
    private SagaOrchestrator sagaOrchestrator;

    @Autowired
    private UserService userService;

    @PostMapping("/user-registration")
    public ResponseEntity<Map<String, String>> startUserRegistrationSaga(@RequestBody User user) {
        String correlationId = UUID.randomUUID().toString();
        String sagaId = sagaOrchestrator.startUserRegistrationSaga(correlationId);

        // Start the first step (user creation) with saga context
        try {
            userService.saveWithSaga(user, sagaId);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new HashMap<String, String>() {
                {
                    put("error", "Failed to start user registration saga");
                    put("message", e.getMessage());
                    put("sagaId", sagaId);
                }
            });
        }

        Map<String, String> response = new HashMap<>();
        response.put("sagaId", sagaId);
        response.put("correlationId", correlationId);
        response.put("status", "STARTED");
        response.put("message", "User registration saga started successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{sagaId}/status")
    public ResponseEntity<SagaState> getSagaStatus(@PathVariable String sagaId) {
        SagaState sagaState = sagaOrchestrator.getSagaState(sagaId);

        if (sagaState == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(sagaState);
    }

    @GetMapping("/{sagaId}/steps")
    public ResponseEntity<Map<String, Object>> getSagaSteps(@PathVariable String sagaId) {
        SagaState sagaState = sagaOrchestrator.getSagaState(sagaId);

        if (sagaState == null) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("sagaId", sagaId);
        response.put("status", sagaState.getStatus());
        response.put("steps", sagaState.getSteps());
        response.put("currentStep", sagaState.getCurrentStep());
        response.put("compensationRequired", sagaState.isCompensationRequired());
        return ResponseEntity.ok(response);
    }
}
