package com.gyan.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.gyan.entity.Chat;
import com.gyan.entity.Document;
import com.gyan.entity.User;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    Page<Document> findByUser(User user, Pageable pageable);

    Page<Document> findByChat(Chat chat, Pageable pageable);

    long countByChat(Chat chat);

    java.util.List<Document> findAllByChat(Chat chat);

    void deleteByChat(Chat chat);
}
