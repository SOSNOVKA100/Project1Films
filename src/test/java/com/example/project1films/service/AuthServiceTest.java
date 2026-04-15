package com.example.project1films.service;

import com.example.project1films.dto.request.LoginRequest;
import com.example.project1films.dto.request.RegisterRequest;
import com.example.project1films.entity.User;
import com.example.project1films.exception.DuplicateResourceException;
import com.example.project1films.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private RegisterRequest validRegisterRequest;
    private LoginRequest validLoginRequest;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        validRegisterRequest = new RegisterRequest();
        validRegisterRequest.setName("John Doe");
        validRegisterRequest.setEmail("john@example.com");
        validRegisterRequest.setPassword("password123");
        validRegisterRequest.setRole("USER");

        validLoginRequest = new LoginRequest();
        validLoginRequest.setEmail("john@example.com");
        validLoginRequest.setPassword("password123");
    }

    // ================= success cases =================

    @Test
    void register_Success_ShouldReturnToken() {
        String token = authService.register(validRegisterRequest);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();

        User savedUser = userRepository.findByEmail("john@example.com").orElse(null);
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getName()).isEqualTo("John Doe");
        assertThat(savedUser.getEmail()).isEqualTo("john@example.com");
        assertThat(savedUser.getRole()).isEqualTo("USER");
    }

    @Test
    void login_Success_ShouldReturnToken() throws Exception {
        authService.register(validRegisterRequest);

        String token = authService.login(validLoginRequest.getEmail(), validLoginRequest.getPassword());

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }

    // ================= error registration =================

    @Test
    void register_Fail_WhenEmailAlreadyExists() {
        authService.register(validRegisterRequest);

        assertThatThrownBy(() -> authService.register(validRegisterRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void register_Fail_WhenNameIsEmpty() {
        RegisterRequest invalidRequest = new RegisterRequest();
        invalidRequest.setName("");
        invalidRequest.setEmail("test@example.com");
        invalidRequest.setPassword("password123");

        assertThatThrownBy(() -> authService.register(invalidRequest))
                .isInstanceOf(Exception.class);
    }

    @Test
    void register_Fail_WhenEmailIsInvalid() {
        RegisterRequest invalidRequest = new RegisterRequest();
        invalidRequest.setName("Test User");
        invalidRequest.setEmail("invalid-email");
        invalidRequest.setPassword("password123");

        assertThatThrownBy(() -> authService.register(invalidRequest))
                .isInstanceOf(Exception.class);
    }

    @Test
    void register_Fail_WhenPasswordTooShort() {
        RegisterRequest invalidRequest = new RegisterRequest();
        invalidRequest.setName("Test User");
        invalidRequest.setEmail("test@example.com");
        invalidRequest.setPassword("123");

        assertThatThrownBy(() -> authService.register(invalidRequest))
                .isInstanceOf(Exception.class);
    }

    // ================= ERROR LOGIN =================

    @Test
    void login_Fail_WhenUserNotFound() {
        assertThatThrownBy(() -> authService.login("nonexistent@example.com", "password123"))
                .isInstanceOf(Exception.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void login_Fail_WhenPasswordIsWrong() {
        authService.register(validRegisterRequest);

        assertThatThrownBy(() -> authService.login("john@example.com", "wrongpassword"))
                .isInstanceOf(Exception.class)
                .hasMessageContaining("Invalid password");
    }

    @Test
    void login_Fail_WhenEmailIsEmpty() {
        assertThatThrownBy(() -> authService.login("", "password123"))
                .isInstanceOf(Exception.class);
    }

    // ================= SEQUENCE(последоавтельность) CASES =================

    @Test
    void fullAuthFlow_Success() throws Exception {
        // 1. Registration
        String registerToken = authService.register(validRegisterRequest);
        assertThat(registerToken).isNotNull();
        System.out.println("1. Registration successful");

        // 2. checked DB
        User user = userRepository.findByEmail("john@example.com").orElse(null);
        assertThat(user).isNotNull();
        System.out.println("2. User found in database");

        // 3. login
        String loginToken = authService.login("john@example.com", "password123");
        assertThat(loginToken).isNotNull();
        System.out.println("3. Login successful");

        // 4. check password
        assertThat(passwordEncoder.matches("password123", user.getPassword())).isTrue();
        System.out.println("4. Password encrypted correctly");
    }

    @Test
    void authFlow_FailThenSuccess() throws Exception {
        // 1. non-existent user
        assertThatThrownBy(() -> authService.login("new@example.com", "password123"))
                .isInstanceOf(Exception.class);
        System.out.println("1. Login with non-existent user - FAILED (expected)");

        // 2. succes register
        authService.register(validRegisterRequest);
        System.out.println("2. Registration - SUCCESS");

        // 3. login after registrarion
        String token = authService.login("john@example.com", "password123");
        assertThat(token).isNotNull();
        System.out.println("3. Login after registration - SUCCESS");
    }

    @Test
    void authFlow_WrongPasswordThenCorrect() throws Exception {
        authService.register(validRegisterRequest);

        // 1. incorrect password - error
        assertThatThrownBy(() -> authService.login("john@example.com", "wrongpassword"))
                .isInstanceOf(Exception.class);
        System.out.println("1. Login with wrong password - FAILED (expected)");

        // 2. succes password
        String token = authService.login("john@example.com", "password123");
        assertThat(token).isNotNull();
        System.out.println("2. Login with correct password - SUCCESS");
    }

    @Test
    void register_DuplicateEmail_Fail() {
        authService.register(validRegisterRequest);
        System.out.println("1. First registration - SUCCESS");

        assertThatThrownBy(() -> authService.register(validRegisterRequest))
                .isInstanceOf(DuplicateResourceException.class);
        System.out.println("2. Second registration with same email - FAILED (expected)");
    }
}