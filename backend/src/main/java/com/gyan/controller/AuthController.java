package com.gyan.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gyan.dto.AuthResponse;
import com.gyan.dto.LoginRequestDTO;
import com.gyan.dto.UserRequestDTO;
import com.gyan.dto.UserResponseDTO;
import com.gyan.service.UserService;

import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private static final String REFRESH_TOKEN_COOKIE = "refresh_token";
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService; 
    }
    
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequestDTO request) {
        ResponseEntity<AuthResponse> response = userService.login(request);
        return withRefreshCookie(response.getBody());
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(HttpServletRequest request) {
        String refreshToken = getRefreshTokenFromCookie(request);
        ResponseEntity<AuthResponse> response = userService.refresh(refreshToken);
        return withRefreshCookie(response.getBody());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String refreshToken = getRefreshTokenFromCookie(request);
        userService.logout(refreshToken);
        return ResponseEntity.noContent()
            .header("Set-Cookie", expiredRefreshCookie().toString())
            .build();
    }

    @PostMapping("/register")
    public UserResponseDTO register(@Valid @RequestBody UserRequestDTO request){
        return userService.createUser(request);
    }

    private ResponseEntity<AuthResponse> withRefreshCookie(AuthResponse body) {
        return ResponseEntity.ok()
            .header("Set-Cookie", refreshCookie(body.getRefreshToken()).toString())
            .body(new AuthResponse(body.getAccessToken(), null));
    }

    private ResponseCookie refreshCookie(String refreshToken) {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE, refreshToken)
            .httpOnly(true)
            .secure(false)
            .sameSite("Lax")
            .path("/")
            .maxAge(7 * 24 * 60 * 60)
            .build();
    }

    private ResponseCookie expiredRefreshCookie() {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE, "")
            .httpOnly(true)
            .secure(false)
            .sameSite("Lax")
            .path("/")
            .maxAge(0)
            .build();
    }

    private String getRefreshTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            throw new RuntimeException("Refresh token is required");
        }

        for (var cookie : request.getCookies()) {
            if (REFRESH_TOKEN_COOKIE.equals(cookie.getName()) && cookie.getValue() != null && !cookie.getValue().isBlank()) {
                return cookie.getValue();
            }
        }

        throw new RuntimeException("Refresh token is required");
    }
}
