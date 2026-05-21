package com.gyan.repository;

import java.util.List;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.gyan.search.DocumentIndex;

public interface DocumentSearchRepository extends ElasticsearchRepository<DocumentIndex, Long> {
    List<DocumentIndex> findByContentContainingAndOwnerEmail(String keyword, String ownerEmail);
}
