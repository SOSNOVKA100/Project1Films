package com.example.project1films.service;

import com.example.project1films.dto.request.UserCreateRequest;
import com.example.project1films.dto.request.UserUpdateRequest;
import com.example.project1films.dto.response.UserResponse;
import com.example.project1films.entity.User;
import com.example.project1films.repository.UserRepository;
import com.example.project1films.security.EncryptionService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
                           EncryptionService encryptionService,
                           PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserResponse getUser(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return mapToResponse(user);
    }

    @Override
    public Page<UserResponse> getUsers(
            String role,
            String search,
            Pageable pageable) {

        Specification<User> spec = (root, query, cb) -> cb.conjunction();
        if (role != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("role"), role));
        }

        if (search != null) {
            spec = spec.and((root, query, cb) ->
                    cb.like(
                            cb.lower(root.get("name")),
                            "%" + search.toLowerCase() + "%"
                    ));
        }

        return userRepository.findAll(spec, pageable)
                .map(this::mapToResponse);
    }




    @Override
    public void deleteUser(Long id) {

        userRepository.deleteById(id);
    }

    private UserResponse mapToResponse(User user) {

        UserResponse response = new UserResponse();

        response.setId(user.getId());
        response.setName(user.getName());

        response.setEmail(user.getEmail());

        response.setRole(user.getRole());

        return response;
    }

    @Override
    public UserResponse createUser(UserCreateRequest request) {

        User user = new User();
        user.setName(request.getName());
        user.setRole(
                request.getRole() != null ? request.getRole() : "USER"
        );

        // Шифруем email
        user.setEmail(request.getEmail());
        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        User saved = userRepository.save(user);

        return mapToResponse(saved);
    }

    @Override
    public UserResponse updateUser(
            Long id,
            UserUpdateRequest request) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));



        if (request.getName() != null) {
            user.setName(request.getName());
        }

        if (request.getEmail() != null) {

            //  String encryptedEmail =
             //       encryptionService.encrypt(request.getEmail());

            user.setEmail(request.getEmail());        }

        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }

        if (request.getPassword() != null) {
            user.setPassword(
                    passwordEncoder.encode(request.getPassword())
            );
        }

        User updated = userRepository.save(user);

        return mapToResponse(updated);


    }
}