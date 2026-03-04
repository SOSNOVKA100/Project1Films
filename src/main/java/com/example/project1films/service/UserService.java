package com.example.project1films.service;

import com.example.project1films.dto.request.UserCreateRequest;
import com.example.project1films.dto.request.UserUpdateRequest;
import com.example.project1films.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {

    UserResponse createUser(UserCreateRequest request);

    UserResponse getUser(Long id);

    Page<UserResponse> getUsers(
            String role,
            String search,
            Pageable pageable
    );

    void deleteUser(Long id);

    UserResponse updateUser(Long id, UserUpdateRequest request);
}