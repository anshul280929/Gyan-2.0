package com.gyan.search;

import org.springframework.stereotype.Service;

import com.gyan.repository.DocumentSearchRepository;

@Service
public class ElasticsearchSearchIndexService implements SearchIndexService {
    private final DocumentSearchRepository repository;

    public ElasticsearchSearchIndexService(DocumentSearchRepository repository) {
        this.repository = repository;
    }

    @Override
    public void indexDocument(DocumentIndex documentIndex) {
        repository.save(documentIndex);

        System.out.println("Document Index : " + documentIndex.getDocumentId());

    }

    @Override
    public void deleteDocument(Long documentId) {
        repository.deleteById(documentId);
    }
}
