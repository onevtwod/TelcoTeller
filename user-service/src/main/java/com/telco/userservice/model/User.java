package com.telco.userservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {
    @Id
    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "data_plan_limit", nullable = false)
    private Long dataPlanLimit;

    @Column(name = "current_usage", nullable = false)
    private Long currentUsage;

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
