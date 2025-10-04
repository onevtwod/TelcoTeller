package com.telco.userservice.service;

import com.telco.userservice.mapper.UserMapper;
import com.telco.userservice.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.flyway.enabled=true"
})
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("user001", "+1234567890", 5368709120L, 1073741824L);
    }

    @Test
    void save_WithValidUser_ShouldPersistUser() {
        // When
        User savedUser = userService.save(testUser);

        // Then
        assertNotNull(savedUser);
        assertEquals(testUser.getUserId(), savedUser.getUserId());
        assertEquals(testUser.getPhoneNumber(), savedUser.getPhoneNumber());

        // Verify user was persisted
        Optional<User> foundUser = userService.findById(testUser.getUserId());
        assertTrue(foundUser.isPresent());
        assertEquals(testUser.getUserId(), foundUser.get().getUserId());
    }

    @Test
    void save_WithDuplicateUserId_ShouldThrowException() {
        // Given
        userService.save(testUser);
        User duplicateUser = new User("user001", "+9876543210", 10737418240L, 2147483648L);

        // When & Then
        assertThrows(IllegalStateException.class, () -> userService.save(duplicateUser));
    }

    @Test
    void save_WithDuplicatePhoneNumber_ShouldThrowException() {
        // Given
        userService.save(testUser);
        User duplicatePhoneUser = new User("user002", "+1234567890", 10737418240L, 2147483648L);

        // When & Then
        assertThrows(IllegalStateException.class, () -> userService.save(duplicatePhoneUser));
    }

    @Test
    void update_WithValidUser_ShouldUpdateUser() {
        // Given
        userService.save(testUser);
        testUser.setDataPlanLimit(10737418240L);
        testUser.setCurrentUsage(2147483648L);

        // When
        Optional<User> updatedUser = userService.update(testUser);

        // Then
        assertTrue(updatedUser.isPresent());
        assertEquals(10737418240L, updatedUser.get().getDataPlanLimit());
        assertEquals(2147483648L, updatedUser.get().getCurrentUsage());
    }

    @Test
    void update_WithNonExistentUser_ShouldReturnEmpty() {
        // When
        Optional<User> result = userService.update(testUser);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void updateUsage_WithValidData_ShouldUpdateUsage() {
        // Given
        userService.save(testUser);
        long newUsage = 2000000000L;

        // When
        Optional<User> updatedUser = userService.updateUsage(testUser.getUserId(), newUsage);

        // Then
        assertTrue(updatedUser.isPresent());
        assertEquals(newUsage, updatedUser.get().getCurrentUsage());
    }

    @Test
    void updateUsage_WithNegativeUsage_ShouldThrowException() {
        // Given
        userService.save(testUser);

        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> userService.updateUsage(testUser.getUserId(), -1L));
    }

    @Test
    void findUsersAboveThreshold_ShouldReturnCorrectUsers() {
        // Given
        User user1 = new User("user001", "+1111111111", 1000000000L, 900000000L); // 90%
        User user2 = new User("user002", "+2222222222", 1000000000L, 500000000L); // 50%
        User user3 = new User("user003", "+3333333333", 1000000000L, 950000000L); // 95%

        userService.save(user1);
        userService.save(user2);
        userService.save(user3);

        // When
        List<User> usersAboveThreshold = userService.findUsersAboveThreshold(80.0);

        // Then
        assertEquals(2, usersAboveThreshold.size());
        assertTrue(usersAboveThreshold.stream().anyMatch(u -> u.getUserId().equals("user001")));
        assertTrue(usersAboveThreshold.stream().anyMatch(u -> u.getUserId().equals("user003")));
        assertFalse(usersAboveThreshold.stream().anyMatch(u -> u.getUserId().equals("user002")));
    }

    @Test
    void isUserAboveThreshold_WithUserAboveThreshold_ShouldReturnTrue() {
        // Given
        User user = new User("user001", "+1111111111", 1000000000L, 900000000L); // 90%
        userService.save(user);

        // When
        boolean result = userService.isUserAboveThreshold("user001", 80.0);

        // Then
        assertTrue(result);
    }

    @Test
    void isUserAboveThreshold_WithUserBelowThreshold_ShouldReturnFalse() {
        // Given
        User user = new User("user001", "+1111111111", 1000000000L, 500000000L); // 50%
        userService.save(user);

        // When
        boolean result = userService.isUserAboveThreshold("user001", 80.0);

        // Then
        assertFalse(result);
    }

    @Test
    void isUserOverLimit_WithUserOverLimit_ShouldReturnTrue() {
        // Given
        User user = new User("user001", "+1111111111", 1000000000L, 1100000000L); // 110%
        userService.save(user);

        // When
        boolean result = userService.isUserOverLimit("user001");

        // Then
        assertTrue(result);
    }

    @Test
    void isUserOverLimit_WithUserUnderLimit_ShouldReturnFalse() {
        // Given
        User user = new User("user001", "+1111111111", 1000000000L, 900000000L); // 90%
        userService.save(user);

        // When
        boolean result = userService.isUserOverLimit("user001");

        // Then
        assertFalse(result);
    }

    @Test
    void deleteById_WithExistingUser_ShouldReturnTrue() {
        // Given
        userService.save(testUser);

        // When
        boolean result = userService.deleteById(testUser.getUserId());

        // Then
        assertTrue(result);
        assertFalse(userService.findById(testUser.getUserId()).isPresent());
    }

    @Test
    void deleteById_WithNonExistentUser_ShouldReturnFalse() {
        // When
        boolean result = userService.deleteById("nonexistent");

        // Then
        assertFalse(result);
    }

    @Test
    void count_ShouldReturnCorrectCount() {
        // Given
        userService.save(testUser);
        userService.save(new User("user002", "+2222222222", 1000000000L, 500000000L));

        // When
        long count = userService.count();

        // Then
        assertEquals(2, count);
    }

    @Test
    void countUsersAboveThreshold_ShouldReturnCorrectCount() {
        // Given
        userService.save(new User("user001", "+1111111111", 1000000000L, 900000000L)); // 90%
        userService.save(new User("user002", "+2222222222", 1000000000L, 500000000L)); // 50%
        userService.save(new User("user003", "+3333333333", 1000000000L, 950000000L)); // 95%

        // When
        long count = userService.countUsersAboveThreshold(80.0);

        // Then
        assertEquals(2, count);
    }
}
