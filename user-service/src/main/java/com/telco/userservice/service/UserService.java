package com.telco.userservice.service;

import com.telco.userservice.mapper.UserMapper;
import com.telco.userservice.model.User;
import com.telco.userservice.saga.event.UserCreatedEvent;
import com.telco.userservice.saga.event.UserCreationFailedEvent;
import com.telco.userservice.saga.producer.SagaEventProducer;
import io.micrometer.tracing.annotation.NewSpan;
import io.micrometer.tracing.annotation.SpanTag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SagaEventProducer sagaEventProducer;

    @NewSpan("find-all-users")
    @Cacheable(value = "users", key = "'all'")
    public List<User> findAll() {
        return userMapper.findAll();
    }

    @NewSpan("find-user-by-id")
    @Cacheable(value = "users", key = "#userId")
    public Optional<User> findById(@SpanTag("user.id") String userId) {
        return userMapper.findById(userId);
    }

    public Optional<User> findByPhoneNumber(String phoneNumber) {
        return userMapper.findByPhoneNumber(phoneNumber);
    }

    public List<User> findUsersAboveThreshold(double threshold) {
        return userMapper.findUsersAboveThreshold(threshold);
    }

    @NewSpan("save-user")
    @CacheEvict(value = "users", allEntries = true)
    public User save(@SpanTag("user.id") User user) {
        return saveWithSaga(user, null);
    }

    @NewSpan("save-user-with-saga")
    @CacheEvict(value = "users", allEntries = true)
    public User saveWithSaga(@SpanTag("user.id") User user, String sagaId) {
        String correlationId = sagaId != null ? sagaId : UUID.randomUUID().toString();

        try {
            if (user.getUserId() == null || user.getUserId().isEmpty()) {
                throw new IllegalArgumentException("User ID is required");
            }

            // Check if user already exists
            if (userMapper.findById(user.getUserId()).isPresent()) {
                throw new IllegalStateException("User with ID " + user.getUserId() + " already exists");
            }

            // Check if phone number already exists
            if (userMapper.findByPhoneNumber(user.getPhoneNumber()).isPresent()) {
                throw new IllegalStateException("User with phone number " + user.getPhoneNumber() + " already exists");
            }

            int result = userMapper.insert(user);
            if (result == 0) {
                throw new RuntimeException("Failed to create user");
            }

            // Publish success event for saga
            if (sagaId != null) {
                UserCreatedEvent event = new UserCreatedEvent(sagaId, correlationId, user);
                sagaEventProducer.publishSagaEvent(event);
            }

            return user;

        } catch (Exception e) {
            // Publish failure event for saga
            if (sagaId != null) {
                UserCreationFailedEvent event = new UserCreationFailedEvent(
                        sagaId, correlationId, user.getUserId(), e.getMessage(), "USER_CREATION_ERROR");
                sagaEventProducer.publishSagaEvent(event);
            }
            throw e;
        }
    }

    public Optional<User> update(User user) {
        if (user.getUserId() == null || user.getUserId().isEmpty()) {
            throw new IllegalArgumentException("User ID is required");
        }

        // Check if user exists
        if (!userMapper.findById(user.getUserId()).isPresent()) {
            return Optional.empty();
        }

        int result = userMapper.update(user);
        if (result == 0) {
            throw new RuntimeException("Failed to update user");
        }

        return Optional.of(user);
    }

    public Optional<User> updateUsage(String userId, long currentUsage) {
        if (currentUsage < 0) {
            throw new IllegalArgumentException("Current usage cannot be negative");
        }

        // Check if user exists
        Optional<User> existingUser = userMapper.findById(userId);
        if (!existingUser.isPresent()) {
            return Optional.empty();
        }

        int result = userMapper.updateUsage(userId, currentUsage);
        if (result == 0) {
            throw new RuntimeException("Failed to update user usage");
        }

        // Return updated user
        return userMapper.findById(userId);
    }

    public boolean deleteById(String userId) {
        return userMapper.deleteById(userId) > 0;
    }

    public long count() {
        return userMapper.count();
    }

    public long countUsersAboveThreshold(double threshold) {
        return userMapper.countUsersAboveThreshold(threshold);
    }

    // Business logic methods
    public List<User> getUsersNeedingAlerts(double threshold) {
        return findUsersAboveThreshold(threshold);
    }

    public boolean isUserAboveThreshold(String userId, double threshold) {
        return findById(userId)
                .map(user -> user.isAboveThreshold(threshold))
                .orElse(false);
    }

    public boolean isUserOverLimit(String userId) {
        return findById(userId)
                .map(User::isOverLimit)
                .orElse(false);
    }
}
