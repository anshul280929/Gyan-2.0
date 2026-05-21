package com.gyan.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.gyan.entity.Document;
import com.gyan.processing.DocumentTextExtractionService;
import com.gyan.repository.DocumentChunkRepository;
import com.gyan.repository.DocumentRepository;
import com.gyan.search.DocumentIndex;
import com.gyan.search.SearchIndexService;
import com.gyan.model.DocumentProcessingStatus;

@ExtendWith(MockitoExtension.class)
public class DocumentProcessingServiceTest {
    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private DocumentTextExtractionService extractionService;

    @Mock
    private DocumentIndexService indexService;

    @Mock
    private SearchIndexService searchIndexService;

    @Mock
    private EmbeddingService embeddingService;

    @Mock
    private DocumentChunkRepository documentChunkRepository;

    @InjectMocks 
    private DocumentProcessingService documentProcessingService;

    @Test
    void testProcessDocument() throws Exception {
        Long docId = 10L;

        Document doc = new Document();
        doc.setId(docId);

        when(documentRepository.findById(docId))
            .thenReturn(Optional.of(doc));
        
        when(extractionService.extractText(any()))
            .thenReturn("Kafka is used for event driven systems");

        when(embeddingService.generateEmbedding(any()))
            .thenReturn(List.of(0.1, 0.2, 0.3));
            
        when(indexService.buildIndex(any()))
            .thenReturn(new DocumentIndex(docId, "new.txt", "Kafka is used for event driven systems", "anam@gmail.com"));
        
        documentProcessingService.processDocument(docId, "filePath", "text/plain");
        
        verify(documentRepository, atLeastOnce()).save(any(Document.class));
        verify(documentChunkRepository, atLeastOnce()).save(any());
        verify(searchIndexService).indexDocument(any());
        assertEquals(DocumentProcessingStatus.READY, doc.getProcessingStatus());
        assertEquals("Ready for preview, search, and chat.", doc.getProcessingMessage());
        assertNotNull(doc.getProcessingStartedAt());
        assertNotNull(doc.getProcessingCompletedAt());
    }

    @Test
    void testProcessDocumentMarksFailureDetails() throws Exception {
        Long docId = 11L;

        Document doc = new Document();
        doc.setId(docId);

        when(documentRepository.findById(docId)).thenReturn(Optional.of(doc));
        when(extractionService.extractText(any())).thenThrow(new RuntimeException("Text extraction failed"));

        documentProcessingService.processDocument(docId, "filePath", "text/plain");

        verify(documentRepository, atLeastOnce()).save(any(Document.class));
        verify(documentChunkRepository, times(0)).save(any());
        verify(searchIndexService, times(0)).indexDocument(any());

        assertEquals(DocumentProcessingStatus.FAILED, doc.getProcessingStatus());
        assertEquals("Processing failed.", doc.getProcessingMessage());
        assertEquals("Text extraction failed", doc.getProcessingError());
        assertNotNull(doc.getProcessingStartedAt());
        assertNotNull(doc.getProcessingCompletedAt());
    }
}
