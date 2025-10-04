package com.telco.userservice.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.telco.userservice.model.User;
import com.telco.userservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("user001", "+1234567890", 5368709120L, 1073741824L);
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() throws Exception {
        // Given
        List<User> users = Arrays.asList(testUser);
        when(userService.findAll()).thenReturn(users);

        // When & Then
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].userId").value("user001"))
                .andExpect(jsonPath("$[0].phoneNumber").value("+1234567890"))
                .andExpect(jsonPath("$[0].dataPlanLimit").value(5368709120L))
                .andExpect(jsonPath("$[0].currentUsage").value(1073741824L));
    }

    @Test
    void getUserById_WhenUserExists_ShouldReturnUser() throws Exception {
        // Given
        when(userService.findById("user001")).thenReturn(Optional.of(testUser));

        // When & Then
        mockMvc.perform(get("/api/v1/users/user001"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value("user001"))
                .andExpect(jsonPath("$.phoneNumber").value("+1234567890"));
    }

    @Test
    void getUserById_WhenUserNotExists_ShouldReturn404() throws Exception {
        // Given
        when(userService.findById("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/v1/users/nonexistent"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createUser_WithValidData_ShouldReturn201() throws Exception {
        // Given
        when(userService.save(any(User.class))).thenReturn(testUser);

        // When & Then
        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value("user001"));
    }

    @Test
    void createUser_WithInvalidData_ShouldReturn400() throws Exception {
        // Given
        User invalidUser = new User("", "", -1L, -1L);

        // When & Then
        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void updateUser_WithValidData_ShouldReturn200() throws Exception {
        // Given
        when(userService.update(any(User.class))).thenReturn(Optional.of(testUser));

        // When & Then
        mockMvc.perform(put("/api/v1/users/user001")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value("user001"));
    }

    @Test
    void updateUser_WhenUserNotExists_ShouldReturn404() throws Exception {
        // Given
        when(userService.update(any(User.class))).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(put("/api/v1/users/nonexistent")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateUserUsage_WithValidData_ShouldReturn200() throws Exception {
        // Given
        when(userService.updateUsage("user001", 2000000000L)).thenReturn(Optional.of(testUser));

        // When & Then
        mockMvc.perform(put("/api/v1/users/user001/usage")
                .param("currentUsage", "2000000000"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value("user001"));
    }

    @Test
    void updateUserUsage_WithNegativeUsage_ShouldReturn400() throws Exception {
        // When & Then
        mockMvc.perform(put("/api/v1/users/user001/usage")
                .param("currentUsage", "-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteUser_WhenUserExists_ShouldReturn204() throws Exception {
        // Given
        when(userService.deleteById("user001")).thenReturn(true);

        // When & Then
        mockMvc.perform(delete("/api/v1/users/user001"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteUser_WhenUserNotExists_ShouldReturn404() throws Exception {
        // Given
        when(userService.deleteById("nonexistent")).thenReturn(false);

        // When & Then
        mockMvc.perform(delete("/api/v1/users/nonexistent"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUsersAboveThreshold_WithValidThreshold_ShouldReturnUsers() throws Exception {
        // Given
        List<User> users = Arrays.asList(testUser);
        when(userService.findUsersAboveThreshold(80.0)).thenReturn(users);

        // When & Then
        mockMvc.perform(get("/api/v1/users/threshold/80.0"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].userId").value("user001"));
    }

    @Test
    void getUsersAboveThreshold_WithInvalidThreshold_ShouldReturn400() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/users/threshold/-10.0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserStats_ShouldReturnStatistics() throws Exception {
        // Given
        when(userService.count()).thenReturn(100L);
        when(userService.countUsersAboveThreshold(80.0)).thenReturn(25L);

        // When & Then
        mockMvc.perform(get("/api/v1/users/stats"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalUsers").value(100))
                .andExpect(jsonPath("$.usersAboveThreshold").value(25));
    }
}
