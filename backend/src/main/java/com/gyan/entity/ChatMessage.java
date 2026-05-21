package com.gyan.entity;

import java.time.LocalDateTime;

import com.gyan.model.ChatMessageRole;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "chat_messages")
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "chat_id", nullable = false)
    private Chat chat;

    @Enumerated(EnumType.STRING)
    private ChatMessageRole role;

    private LocalDateTime createdAt;

    @jakarta.persistence.Column(columnDefinition = "TEXT")
    private String content;

    public Long getId() {
        return id;
    }

    public Chat getChat() {
        return chat;
    }

    public ChatMessageRole getRole() {
        return role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getContent() {
        return content;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setChat(Chat chat) {
        this.chat = chat;
    }

    public void setRole(ChatMessageRole role) {
        this.role = role;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
