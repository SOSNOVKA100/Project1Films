package com.example.project1films.service;

import com.example.project1films.entity.User;
import java.util.List;

public interface UserService {

    List<User> getAllUsers();

    User getUserById(Long id);

    User createUser(User user);

    User updateUser(Long id, User user);

    void deleteUser(Long id);

    String decryptEmail(String email);
}
