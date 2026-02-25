package com.example.project1films.controller;

import com.example.project1films.entity.User;
import com.example.project1films.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users2")
public class UserControllerWithService {

    private final UserService userService;

    public UserControllerWithService(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @GetMapping("/{id}/decoded")
    public User getUserDecoded(@PathVariable Long id) {
        User user = userService.getUserById(id);

        User decoded = new User();
        decoded.setId(user.getId());
        decoded.setName(user.getName());
        decoded.setRole(user.getRole());
        decoded.setEmail(userService.decryptEmail(user.getEmail()));

        return decoded;
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        return userService.createUser(user);
    }


    @PutMapping("/{id}")
    public User updateUser(@PathVariable Long id, @RequestBody User user) {
        return userService.updateUser(id, user);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }


}
