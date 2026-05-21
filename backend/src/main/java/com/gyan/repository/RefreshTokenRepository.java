package com.gyan.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gyan.entity.RefreshToken;
import com.gyan.entity.User;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    List<RefreshToken> findByUserAndRevokedFalse(User user);
}
