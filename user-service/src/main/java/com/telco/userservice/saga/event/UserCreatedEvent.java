package com.telco.userservice.saga.event;

import com.telco.userservice.model.User;

public class UserCreatedEvent extends SagaEvent {
    private User user;
    private String userId;
    private String phoneNumber;
    private Long dataPlanLimit;
    private Long currentUsage;

    public UserCreatedEvent() {
        super();
        setEventType("USER_CREATED");
    }

    public UserCreatedEvent(String sagaId, String correlationId, User user) {
        super(sagaId, correlationId);
        setEventType("USER_CREATED");
        this.user = user;
        this.userId = user.getUserId();
        this.phoneNumber = user.getPhoneNumber();
        this.dataPlanLimit = user.getDataPlanLimit();
        this.currentUsage = user.getCurrentUsage();
    }

    // Getters and Setters
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Long getDataPlanLimit() {
        return dataPlanLimit;
    }

    public void setDataPlanLimit(Long dataPlanLimit) {
        this.dataPlanLimit = dataPlanLimit;
    }

    public Long getCurrentUsage() {
        return currentUsage;
    }

    public void setCurrentUsage(Long currentUsage) {
        this.currentUsage = currentUsage;
    }
}
