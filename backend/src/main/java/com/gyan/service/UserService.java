package com.gyan.service;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gyan.dto.AuthResponse;
import com.gyan.dto.LoginRequestDTO;
import com.gyan.dto.UserRequestDTO;
import com.gyan.dto.UserResponseDTO;
import com.gyan.entity.User;
import com.gyan.exception.BadRequestException;
import com.gyan.model.Role;
import com.gyan.repository.UserRepository;
import com.gyan.security.JwtUtil;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final AuditLogService auditLogService;

    public UserService(
        UserRepository userRepository,
        BCryptPasswordEncoder passwordEncoder,
        RefreshTokenService refreshTokenService,
        AuditLogService auditLogService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenService = refreshTokenService;
        this.auditLogService = auditLogService;
    }

    public UserResponseDTO createUser(UserRequestDTO request) { 
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            auditLogService.logAuth("auth.register", request.getEmail(), "FAILED");
            throw new BadRequestException("An account with this email already exists.");
        }

        String hashedPassword = passwordEncoder.encode(request.getPassword());

        User user = new User(
            request.getEmail(),
            hashedPassword,
            Role.USER
        );
        
        User savedUser = userRepository.save(user);
        auditLogService.logAuth("auth.register", savedUser.getEmail(), "SUCCESS");
        return new UserResponseDTO(
            savedUser.getId(),
            savedUser.getEmail()
        );
    }

    @Transactional
    public ResponseEntity<AuthResponse> login(LoginRequestDTO request) {
        User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> {
                        auditLogService.logAuth("auth.login", request.getEmail(), "FAILED");
                        return new BadRequestException("Invalid credentials");
                    });

        boolean valid = passwordEncoder.matches(
            request.getPassword(),
            user.getPassword()
        );

        if(!valid) {
            auditLogService.logAuth("auth.login", user.getEmail(), "FAILED");
            throw new BadRequestException("Invalid credentials");
        }

        refreshTokenService.revokeActiveTokens(user);
        String accessToken = JwtUtil.generateToken(user.getEmail(), user.getRole());
        String refreshToken = refreshTokenService.createRefreshToken(user);
        auditLogService.logAuth("auth.login", user.getEmail(), "SUCCESS");

        return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken));
    }

    @Transactional
    public ResponseEntity<AuthResponse> refresh(String refreshTokenValue) {
        var existingToken = refreshTokenService.validateRefreshToken(refreshTokenValue);
        User user = existingToken.getUser();

        refreshTokenService.revokeToken(refreshTokenValue);
        String accessToken = JwtUtil.generateToken(user.getEmail(), user.getRole());
        String refreshToken = refreshTokenService.createRefreshToken(user);
        auditLogService.logAuth("auth.refresh", user.getEmail(), "SUCCESS");

        return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken));
    }

    @Transactional
    public ResponseEntity<Void> logout(String refreshTokenValue) {
        try {
            var token = refreshTokenService.validateRefreshToken(refreshTokenValue);
            auditLogService.logAuth("auth.logout", token.getUser().getEmail(), "SUCCESS");
        } catch (RuntimeException ignored) {
            auditLogService.logAuth("auth.logout", "unknown", "FAILED");
        }
        refreshTokenService.revokeToken(refreshTokenValue);
        return ResponseEntity.noContent().build();
    }

    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll()
        .stream()
        .map(user -> new UserResponseDTO(
            user.getId(),
            user.getEmail()
        )).toList();
    }

    public UserResponseDTO getUser(Long id){
        User user = userRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("User not found"));

        return new UserResponseDTO(
            user.getId(),
            user.getEmail()
        );
    }

    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }
}
