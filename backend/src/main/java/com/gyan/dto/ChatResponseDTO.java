package com.gyan.dto;

import java.time.LocalDateTime;

public class ChatResponseDTO {
    private Long id;
    private String name;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long documentCount;

    public ChatResponseDTO(Long id, String name, LocalDateTime createdAt, LocalDateTime updatedAt, Long documentCount) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.documentCount = documentCount;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Long getDocumentCount() {
        return documentCount;
    }
}
