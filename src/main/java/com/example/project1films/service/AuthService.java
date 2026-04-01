package com.example.project1films.service;

import com.example.project1films.dto.request.RegisterRequest;
import com.example.project1films.dto.response.UserResponse;
import com.example.project1films.entity.User;
import com.example.project1films.exception.DuplicateResourceException;
import com.example.project1films.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AsyncNotificationService asyncNotificationService;
    private final SecretKey secretKey;
    private final long EXPIRATION_TIME = 86400000; // 24 hours

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AsyncNotificationService asyncNotificationService,
                       @Value("${jwt.secret}") String secret) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.asyncNotificationService = asyncNotificationService;
        this.secretKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret));
    }

    @Transactional
    public String register(RegisterRequest request) {
        logger.info("Registering new user with email: {}", request.getEmail());

        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser.isPresent()) {
            throw new DuplicateResourceException("User with email " + request.getEmail() + " already exists");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole() != null ? request.getRole() : "USER");

        User savedUser = userRepository.save(user);
        logger.info("User registered successfully with id: {}, role: {}", savedUser.getId(), savedUser.getRole());

        UserResponse userResponse = mapToUserResponse(savedUser);
        asyncNotificationService.sendWelcomeEmailAsync(userResponse);
        asyncNotificationService.logUserActionAsync(
                savedUser.getId(),
                "REGISTER",
                "New user registered with role: " + savedUser.getRole()
        );

        return generateToken(savedUser.getEmail(), savedUser.getRole());
    }

    public String login(String email, String rawPassword) throws Exception {
        logger.info("Login attempt for email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new Exception("User not found"));

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new Exception("Invalid password");
        }

        logger.info("User logged in successfully: {}, role: {}", email, user.getRole());

        return generateToken(user.getEmail(), user.getRole());
    }

    private String generateToken(String email, String role) {
        logger.info("Generating token for email: {}, role: {}", email, role);

        String token = Jwts.builder()
                .setSubject(email)
                .claim("role", role)  // ← ДОБАВЛЯЕМ РОЛЬ
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(secretKey)
                .compact();

        logger.info("Token generated successfully");
        return token;
    }

    private UserResponse mapToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        return response;
    }
}