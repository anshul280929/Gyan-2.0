package com.gyan.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.gyan.repository.DocumentSearchRepository;
import com.gyan.search.DocumentIndex;

@Service
public class DocumentSearchService {
    private final DocumentSearchRepository repository;

    public DocumentSearchService(DocumentSearchRepository repository) {
        this.repository = repository;
    }

    public List<DocumentIndex> search(String query, String userEmail) {
        return repository.findByContentContainingAndOwnerEmail(query, userEmail);
    }
}
