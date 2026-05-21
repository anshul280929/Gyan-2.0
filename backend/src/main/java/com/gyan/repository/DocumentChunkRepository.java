package com.gyan.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gyan.entity.Document;
import com.gyan.entity.DocumentChunk;

public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, Long> {
    List<DocumentChunk> findByDocument(Document document);

    void deleteByDocument(Document document);

    void deleteByDocumentIn(List<Document> documents);
}
