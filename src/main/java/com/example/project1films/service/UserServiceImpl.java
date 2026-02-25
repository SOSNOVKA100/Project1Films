package com.example.project1films.service;

import com.example.project1films.entity.User;
import com.example.project1films.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    @Override
    public User createUser(User user) {
        user.setEmail(encryptEmail(user.getEmail()));
        return userRepository.save(user);
    }

    @Override
    public User updateUser(Long id, User updatedUser) {
        User existing = getUserById(id);
        existing.setName(updatedUser.getName());
        existing.setRole(updatedUser.getRole());
        existing.setEmail(encryptEmail(updatedUser.getEmail()));
        return userRepository.save(existing);
    }

    @Override
    public void deleteUser(Long id) {
        User existing = getUserById(id);
        userRepository.delete(existing);
    }

    public String encryptEmail(String email) {
        return Base64.getEncoder().encodeToString(email.getBytes());
    }

    public String decryptEmail(String encryptedEmail) {
        return new String(Base64.getDecoder().decode(encryptedEmail));
    }
}
