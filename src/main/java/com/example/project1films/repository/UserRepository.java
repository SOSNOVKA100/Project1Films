package com.example.project1films.repository;

import com.example.project1films.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);//Ошибка совпадения почты
}
