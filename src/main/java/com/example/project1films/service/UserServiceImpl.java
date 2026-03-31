package com.example.project1films.service;

import com.example.project1films.dto.request.UserCreateRequest;
import com.example.project1films.dto.request.UserUpdateRequest;
import com.example.project1films.dto.response.UserResponse;
import com.example.project1films.entity.User;
import com.example.project1films.exception.DuplicateResourceException;
import com.example.project1films.exception.ResourceNotFoundException;
import com.example.project1films.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AsyncNotificationService asyncNotificationService;

    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           AsyncNotificationService asyncNotificationService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.asyncNotificationService = asyncNotificationService;
    }

    @Override
    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        logger.info("Creating user with email: {}", request.getEmail());

        // Проверка на дубликат email
        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser.isPresent()) {
            throw new DuplicateResourceException("User with email " + request.getEmail() + " already exists");
        }

        User user = new User();
        user.setName(request.getName());
        user.setRole(request.getRole() != null ? request.getRole() : "USER");
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        User saved = userRepository.save(user);
        logger.info("User created successfully with id: {}", saved.getId());

        UserResponse response = mapToResponse(saved);

        asyncNotificationService.sendWelcomeEmailAsync(response);

        asyncNotificationService.logUserActionAsync(
                saved.getId(),
                "CREATE_USER",
                "User created with role: " + saved.getRole()
        );

        return response;
    }

    @Override
    public UserResponse getUser(Long id) {
        logger.debug("Fetching user with id: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        return mapToResponse(user);
    }

    @Override
    public Page<UserResponse> getUsers(String role, String search, Pageable pageable) {
        logger.debug("Fetching users with role: {}, search: {}", role, search);

        Specification<User> spec = (root, query, cb) -> cb.conjunction();

        if (role != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("role"), role));
        }

        if (search != null) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("name")), "%" + search.toLowerCase() + "%"));
        }

        return userRepository.findAll(spec, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        logger.info("Deleting user with id: {}", id);

        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }

        userRepository.deleteById(id);
        logger.info("User deleted successfully with id: {}", id);

        asyncNotificationService.logUserActionAsync(id, "DELETE_USER", "User deleted");
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        logger.info("Updating user with id: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        StringBuilder changes = new StringBuilder();

        if (request.getName() != null) {
            changes.append("Name changed from ").append(user.getName()).append(" to ").append(request.getName()).append("; ");
            user.setName(request.getName());
        }

        if (request.getEmail() != null) {
            Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
            if (existingUser.isPresent() && !existingUser.get().getId().equals(id)) {
                throw new DuplicateResourceException("Email " + request.getEmail() + " is already taken");
            }

            changes.append("Email changed from ").append(user.getEmail()).append(" to ").append(request.getEmail()).append("; ");
            user.setEmail(request.getEmail());
        }

        if (request.getRole() != null) {
            changes.append("Role changed from ").append(user.getRole()).append(" to ").append(request.getRole()).append("; ");
            user.setRole(request.getRole());
        }

        if (request.getPassword() != null) {
            changes.append("Password changed; ");
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        User updated = userRepository.save(user);
        logger.info("User updated successfully with id: {}", id);

        UserResponse response = mapToResponse(updated);

        if (changes.length() > 0) {
            asyncNotificationService.sendAccountUpdateNotificationAsync(response, changes.toString());
        }

        asyncNotificationService.logUserActionAsync(id, "UPDATE_USER", changes.toString());

        return response;
    }

    private UserResponse mapToResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        return response;
    }
}