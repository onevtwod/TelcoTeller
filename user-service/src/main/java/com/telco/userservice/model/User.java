package com.telco.userservice.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;

public class User {

    @NotBlank(message = "User ID is required")
    @JsonProperty("user_id")
    private String userId;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    @JsonProperty("phone_number")
    private String phoneNumber;

    @NotNull(message = "Data plan limit is required")
    @Positive(message = "Data plan limit must be positive")
    @JsonProperty("data_plan_limit")
    private Long dataPlanLimit;

    @NotNull(message = "Current usage is required")
    @JsonProperty("current_usage")
    private Long currentUsage;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public User() {
    }

    public User(String userId, String phoneNumber, Long dataPlanLimit, Long currentUsage) {
        this.userId = userId;
        this.phoneNumber = phoneNumber;
        this.dataPlanLimit = dataPlanLimit;
        this.currentUsage = currentUsage;
    }

    // Getters and Setters
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Business logic methods
    public double getUsagePercentage() {
        if (dataPlanLimit == null || dataPlanLimit == 0) {
            return 0.0;
        }
        return (currentUsage.doubleValue() / dataPlanLimit.doubleValue()) * 100.0;
    }

    public boolean isAboveThreshold(double threshold) {
        return getUsagePercentage() >= threshold;
    }

    public boolean isOverLimit() {
        return currentUsage > dataPlanLimit;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", dataPlanLimit=" + dataPlanLimit +
                ", currentUsage=" + currentUsage +
                ", usagePercentage=" + String.format("%.2f", getUsagePercentage()) + "%" +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}