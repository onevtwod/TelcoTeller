package com.telco.userservice.saga.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.telco.userservice.saga.state.SagaState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

@Repository
public class SagaStateRepository {

    private static final Logger logger = LoggerFactory.getLogger(SagaStateRepository.class);
    private static final String SAGA_KEY_PREFIX = "saga:state:";
    private static final Duration SAGA_TTL = Duration.ofHours(24); // 24 hours TTL

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public void saveSagaState(SagaState sagaState) {
        try {
            String key = SAGA_KEY_PREFIX + sagaState.getSagaId();
            String json = objectMapper.writeValueAsString(sagaState);

            redisTemplate.opsForValue().set(key, json, SAGA_TTL);
            logger.debug("Saved saga state for saga: {}", sagaState.getSagaId());

        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize saga state for saga: {}", sagaState.getSagaId(), e);
        }
    }

    public Optional<SagaState> getSagaState(String sagaId) {
        try {
            String key = SAGA_KEY_PREFIX + sagaId;
            String json = redisTemplate.opsForValue().get(key);

            if (json == null) {
                return Optional.empty();
            }

            SagaState sagaState = objectMapper.readValue(json, SagaState.class);
            logger.debug("Retrieved saga state for saga: {}", sagaId);
            return Optional.of(sagaState);

        } catch (JsonProcessingException e) {
            logger.error("Failed to deserialize saga state for saga: {}", sagaId, e);
            return Optional.empty();
        }
    }

    public void deleteSagaState(String sagaId) {
        String key = SAGA_KEY_PREFIX + sagaId;
        redisTemplate.delete(key);
        logger.debug("Deleted saga state for saga: {}", sagaId);
    }

    public boolean existsSagaState(String sagaId) {
        String key = SAGA_KEY_PREFIX + sagaId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public void extendSagaTTL(String sagaId) {
        String key = SAGA_KEY_PREFIX + sagaId;
        redisTemplate.expire(key, SAGA_TTL);
        logger.debug("Extended TTL for saga: {}", sagaId);
    }
}
