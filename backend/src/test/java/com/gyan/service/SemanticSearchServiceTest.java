package com.gyan.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.gyan.entity.Document;
import com.gyan.entity.DocumentChunk;
import com.gyan.repository.DocumentChunkRepository;

@ExtendWith(MockitoExtension.class)
class SemanticSearchServiceTest {

    @Mock
    private DocumentChunkRepository chunkRepository;

    @Mock
    private EmbeddingService embeddingService;

    @InjectMocks
    private SemanticSearchService searchService;

    @Test
    void testFindRelevantChunks() throws Exception {

        Document doc = new Document();
        doc.setId(1L);

        DocumentChunk chunk = new DocumentChunk();
        chunk.setDocument(doc);
        chunk.setEmbeddingVector("[0.1, 0.2, 0.3]");

        when(chunkRepository.findAll())
                .thenReturn(List.of(chunk));

        when(embeddingService.generateEmbedding(any()))
                .thenReturn(List.of(0.1, 0.2, 0.3));

        List<DocumentChunk> result =
                searchService.findRelevantChunks("Kafka");

        assertFalse(result.isEmpty());
    }
}