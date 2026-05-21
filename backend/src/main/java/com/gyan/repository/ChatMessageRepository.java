package com.gyan.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gyan.entity.Chat;
import com.gyan.entity.ChatMessage;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByChatOrderByCreatedAtAsc(Chat chat);
    void deleteByChat(Chat chat);
}
