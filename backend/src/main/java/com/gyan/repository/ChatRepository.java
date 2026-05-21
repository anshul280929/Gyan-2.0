package com.gyan.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gyan.entity.Chat;
import com.gyan.entity.User;

public interface ChatRepository extends JpaRepository<Chat, Long> {
    List<Chat> findByUserOrderByUpdatedAtDesc(User user);

    Optional<Chat> findByIdAndUser(Long id, User user);

    long countByUser(User user);
}
