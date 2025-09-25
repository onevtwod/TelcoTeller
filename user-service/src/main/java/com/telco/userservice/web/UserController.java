package com.telco.userservice.web;

import com.telco.userservice.model.User;
import com.telco.userservice.repo.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<User> createOrUpdate(@RequestBody User user) {
        User saved = userRepository.save(user);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<User> get(@PathVariable String userId) {
        Optional<User> user = userRepository.findById(userId);
        return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{userId}/usage")
    public ResponseEntity<Long> getUsage(@PathVariable String userId) {
        Optional<User> user = userRepository.findById(userId);
        return user.map(u -> ResponseEntity.ok(u.getCurrentUsage()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
