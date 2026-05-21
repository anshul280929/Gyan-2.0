package com.gyan.search;

public interface SearchIndexService {
    void indexDocument(DocumentIndex index);

    void deleteDocument(Long documentId);
}
