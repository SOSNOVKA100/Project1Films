package com.example.project1films.controller;

import com.example.project1films.dto.request.UserCreateRequest;
import com.example.project1films.dto.response.UserResponse;
import com.example.project1films.service.UserService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserControllerWithService {

    private final UserService userService;

    public UserControllerWithService(UserService userService) {

        this.userService = userService;
    }

    @PostMapping
    public UserResponse createUser(@RequestBody UserCreateRequest request) {

        return userService.createUser(request);
    }

    @GetMapping("/{id}")
    public UserResponse getUser(@PathVariable Long id) {

        return userService.getUser(id);
    }


    @GetMapping
    public Page<UserResponse> getUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String search,
            Pageable pageable
    ) {
        return userService.getUsers(role, search, pageable);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {

        userService.deleteUser(id);
    }
}