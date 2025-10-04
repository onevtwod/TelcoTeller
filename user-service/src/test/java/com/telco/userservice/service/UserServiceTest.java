package com.telco.userservice.service;

import com.telco.userservice.mapper.UserMapper;
import com.telco.userservice.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("user001", "+1234567890", 5368709120L, 1073741824L);
    }

    @Test
    void findAll_ShouldReturnAllUsers() {
        // Given
        List<User> expectedUsers = Arrays.asList(testUser);
        when(userMapper.findAll()).thenReturn(expectedUsers);

        // When
        List<User> actualUsers = userService.findAll();

        // Then
        assertEquals(expectedUsers, actualUsers);
        verify(userMapper).findAll();
    }

    @Test
    void findById_WhenUserExists_ShouldReturnUser() {
        // Given
        String userId = "user001";
        when(userMapper.findById(userId)).thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = userService.findById(userId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testUser, result.get());
        verify(userMapper).findById(userId);
    }

    @Test
    void findById_WhenUserNotExists_ShouldReturnEmpty() {
        // Given
        String userId = "nonexistent";
        when(userMapper.findById(userId)).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.findById(userId);

        // Then
        assertFalse(result.isPresent());
        verify(userMapper).findById(userId);
    }

    @Test
    void save_WithValidUser_ShouldSaveUser() {
        // Given
        when(userMapper.findById(testUser.getUserId())).thenReturn(Optional.empty());
        when(userMapper.findByPhoneNumber(testUser.getPhoneNumber())).thenReturn(Optional.empty());
        when(userMapper.insert(testUser)).thenReturn(1);

        // When
        User result = userService.save(testUser);

        // Then
        assertEquals(testUser, result);
        verify(userMapper).insert(testUser);
    }

    @Test
    void save_WithExistingUserId_ShouldThrowException() {
        // Given
        when(userMapper.findById(testUser.getUserId())).thenReturn(Optional.of(testUser));

        // When & Then
        assertThrows(IllegalStateException.class, () -> userService.save(testUser));
        verify(userMapper, never()).insert(any());
    }

    @Test
    void save_WithExistingPhoneNumber_ShouldThrowException() {
        // Given
        when(userMapper.findById(testUser.getUserId())).thenReturn(Optional.empty());
        when(userMapper.findByPhoneNumber(testUser.getPhoneNumber())).thenReturn(Optional.of(testUser));

        // When & Then
        assertThrows(IllegalStateException.class, () -> userService.save(testUser));
        verify(userMapper, never()).insert(any());
    }

    @Test
    void updateUsage_WithValidData_ShouldUpdateUsage() {
        // Given
        String userId = "user001";
        long newUsage = 2000000000L;
        when(userMapper.findById(userId)).thenReturn(Optional.of(testUser));
        when(userMapper.updateUsage(userId, newUsage)).thenReturn(1);
        when(userMapper.findById(userId)).thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = userService.updateUsage(userId, newUsage);

        // Then
        assertTrue(result.isPresent());
        verify(userMapper).updateUsage(userId, newUsage);
    }

    @Test
    void updateUsage_WithNegativeUsage_ShouldThrowException() {
        // Given
        String userId = "user001";
        long negativeUsage = -1L;

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> userService.updateUsage(userId, negativeUsage));
        verify(userMapper, never()).updateUsage(any(), anyLong());
    }

    @Test
    void findUsersAboveThreshold_ShouldReturnFilteredUsers() {
        // Given
        double threshold = 80.0;
        List<User> expectedUsers = Arrays.asList(testUser);
        when(userMapper.findUsersAboveThreshold(threshold)).thenReturn(expectedUsers);

        // When
        List<User> result = userService.findUsersAboveThreshold(threshold);

        // Then
        assertEquals(expectedUsers, result);
        verify(userMapper).findUsersAboveThreshold(threshold);
    }

    @Test
    void isUserAboveThreshold_WhenUserExistsAndAboveThreshold_ShouldReturnTrue() {
        // Given
        String userId = "user001";
        double threshold = 10.0; // User has 20% usage, above 10%
        when(userMapper.findById(userId)).thenReturn(Optional.of(testUser));

        // When
        boolean result = userService.isUserAboveThreshold(userId, threshold);

        // Then
        assertTrue(result);
    }

    @Test
    void isUserAboveThreshold_WhenUserNotExists_ShouldReturnFalse() {
        // Given
        String userId = "nonexistent";
        double threshold = 10.0;
        when(userMapper.findById(userId)).thenReturn(Optional.empty());

        // When
        boolean result = userService.isUserAboveThreshold(userId, threshold);

        // Then
        assertFalse(result);
    }
}
