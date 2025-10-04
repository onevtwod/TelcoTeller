package com.telco.userservice.web;

import com.telco.userservice.model.User;
import com.telco.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/users")
@Tag(name = "User Management", description = "APIs for managing users and their data usage")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieve a list of all users")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved users"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.findAll();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get user by ID", description = "Retrieve a specific user by their ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<User> getUserById(
            @Parameter(description = "User ID", required = true) @PathVariable String userId) {
        Optional<User> user = userService.findById(userId);
        return user.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/phone/{phoneNumber}")
    @Operation(summary = "Get user by phone number", description = "Retrieve a user by their phone number")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<User> getUserByPhoneNumber(
            @Parameter(description = "Phone number", required = true) @PathVariable String phoneNumber) {
        Optional<User> user = userService.findByPhoneNumber(phoneNumber);
        return user.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/threshold/{threshold}")
    @Operation(summary = "Get users above threshold", description = "Retrieve users whose usage is above the specified threshold")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users found"),
            @ApiResponse(responseCode = "400", description = "Invalid threshold value"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<User>> getUsersAboveThreshold(
            @Parameter(description = "Usage threshold percentage", required = true) @PathVariable double threshold) {
        if (threshold < 0 || threshold > 100) {
            return ResponseEntity.badRequest().build();
        }
        List<User> users = userService.findUsersAboveThreshold(threshold);
        return ResponseEntity.ok(users);
    }

    @PostMapping
    @Operation(summary = "Create new user", description = "Create a new user with the provided information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid user data"),
            @ApiResponse(responseCode = "409", description = "User already exists"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
        try {
            User createdUser = userService.save(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @PutMapping("/{userId}")
    @Operation(summary = "Update user", description = "Update an existing user's information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid user data"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<User> updateUser(
            @Parameter(description = "User ID", required = true) @PathVariable String userId,
            @Valid @RequestBody User user) {
        user.setUserId(userId);
        Optional<User> updatedUser = userService.update(user);
        return updatedUser.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{userId}/usage")
    @Operation(summary = "Update user usage", description = "Update a user's current data usage")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usage updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid usage value"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<User> updateUserUsage(
            @Parameter(description = "User ID", required = true) @PathVariable String userId,
            @Parameter(description = "New usage amount in bytes", required = true) @RequestParam long currentUsage) {
        if (currentUsage < 0) {
            return ResponseEntity.badRequest().build();
        }
        Optional<User> updatedUser = userService.updateUsage(userId, currentUsage);
        return updatedUser.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Delete user", description = "Delete a user by their ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "User ID", required = true) @PathVariable String userId) {
        boolean deleted = userService.deleteById(userId);
        return deleted ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @GetMapping("/stats")
    @Operation(summary = "Get user statistics", description = "Get statistics about users and their usage")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<UserStats> getUserStats() {
        long totalUsers = userService.count();
        long usersAboveThreshold = userService.countUsersAboveThreshold(80.0);

        UserStats stats = new UserStats(totalUsers, usersAboveThreshold);
        return ResponseEntity.ok(stats);
    }

    // Inner class for statistics
    public static class UserStats {
        private final long totalUsers;
        private final long usersAboveThreshold;

        public UserStats(long totalUsers, long usersAboveThreshold) {
            this.totalUsers = totalUsers;
            this.usersAboveThreshold = usersAboveThreshold;
        }

        public long getTotalUsers() {
            return totalUsers;
        }

        public long getUsersAboveThreshold() {
            return usersAboveThreshold;
        }
    }
}