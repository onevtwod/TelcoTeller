package com.telco.userservice.web;

import com.telco.userservice.model.User;
import com.telco.userservice.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserListController {
    private final UserService userService;

    public UserListController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<User> list() {
        return userService.findAll();
    }
}
