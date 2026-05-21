package com.gyan.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gyan.entity.RefreshToken;
import com.gyan.entity.User;
import com.gyan.repository.RefreshTokenRepository;

@Service
public class RefreshTokenService {
    private static final long REFRESH_TOKEN_DAYS = 7;

    private final RefreshTokenRepository refreshTokenRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public RefreshTokenService(
        RefreshTokenRepository refreshTokenRepository,
        BCryptPasswordEncoder passwordEncoder
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public String createRefreshToken(User user) {
        String rawSecret = UUID.randomUUID().toString() + UUID.randomUUID().toString();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setCreatedAt(LocalDateTime.now());
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(REFRESH_TOKEN_DAYS));
        refreshToken.setRevoked(false);
        refreshToken.setUser(user);
        refreshToken = refreshTokenRepository.save(refreshToken);

        String rawToken = refreshToken.getId() + "." + rawSecret;
        refreshToken.setTokenHash(passwordEncoder.encode(rawToken));
        refreshTokenRepository.save(refreshToken);

        return rawToken;
    }

    public RefreshToken validateRefreshToken(String tokenValue) {
        Long tokenId = extractTokenId(tokenValue);
        RefreshToken refreshToken = refreshTokenRepository.findById(tokenId)
            .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (refreshToken.isRevoked()) {
            throw new RuntimeException("Refresh token has been revoked");
        }

        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Refresh token has expired");
        }

        if (refreshToken.getTokenHash() == null || !passwordEncoder.matches(tokenValue, refreshToken.getTokenHash())) {
            throw new RuntimeException("Invalid refresh token");
        }

        return refreshToken;
    }

    @Transactional
    public void revokeToken(String tokenValue) {
        try {
            RefreshToken token = validateRefreshToken(tokenValue);
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        } catch (RuntimeException ignored) {
        }
    }

    @Transactional
    public void revokeActiveTokens(User user) {
        for (RefreshToken token : refreshTokenRepository.findByUserAndRevokedFalse(user)) {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        }
    }

    private Long extractTokenId(String tokenValue) {
        String[] parts = tokenValue.split("\\.", 2);

        if (parts.length != 2) {
            throw new RuntimeException("Invalid refresh token");
        }

        try {
            return Long.parseLong(parts[0]);
        } catch (NumberFormatException exception) {
            throw new RuntimeException("Invalid refresh token");
        }
    }
}
