package com.telco.userservice;

import com.telco.userservice.model.User;
import com.telco.userservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureWebMvc
@Testcontainers
@ActiveProfiles("test")
class TestContainersIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("telco_test")
            .withUsername("telco_test")
            .withPassword("telco_test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private UserService userService;

    @Autowired
    private MockMvc mockMvc;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("user001", "+1234567890", 5368709120L, 1073741824L);
    }

    @Test
    void testWithRealDatabase_ShouldWorkCorrectly() {
        // Given
        User user = new User("test001", "+1111111111", 1000000000L, 500000000L);

        // When
        User savedUser = userService.save(user);
        Optional<User> foundUser = userService.findById("test001");

        // Then
        assertNotNull(savedUser);
        assertTrue(foundUser.isPresent());
        assertEquals("test001", foundUser.get().getUserId());
        assertEquals("+1111111111", foundUser.get().getPhoneNumber());
    }

    @Test
    void testFindUsersAboveThreshold_WithRealDatabase() {
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
    }

    @Test
    void testApiEndpoints_WithRealDatabase() throws Exception {
        // Given
        User user = new User("api001", "+9999999999", 2000000000L, 1000000000L);
        userService.save(user);

        // When & Then
        mockMvc.perform(get("/api/v1/users/api001"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/users/threshold/50.0"))
                .andExpect(status().isOk());
    }

    @Test
    void testUserBusinessLogic_WithRealDatabase() {
        // Given
        User user = new User("business001", "+8888888888", 1000000000L, 800000000L); // 80%
        userService.save(user);

        // When & Then
        assertTrue(userService.isUserAboveThreshold("business001", 70.0));
        assertFalse(userService.isUserAboveThreshold("business001", 90.0));
        assertFalse(userService.isUserOverLimit("business001"));

        // Test over limit
        userService.updateUsage("business001", 1100000000L); // 110%
        assertTrue(userService.isUserOverLimit("business001"));
    }
}
