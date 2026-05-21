package com.gyan.service;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gyan.entity.Document;
import com.gyan.entity.DocumentChunk;
import com.gyan.model.DocumentProcessingStatus;
import com.gyan.processing.DocumentTextExtractionService;
import com.gyan.repository.DocumentChunkRepository;
import com.gyan.repository.DocumentRepository;
import com.gyan.search.DocumentIndex;
import com.gyan.search.SearchIndexService;
import com.gyan.util.TextChunker;

@Service
public class DocumentProcessingService {

    private final DocumentRepository documentRepository;
    private final DocumentTextExtractionService extractionService;
    private final DocumentIndexService indexService;
    private final SearchIndexService searchIndexService;
    private final EmbeddingService embeddingService;
    private final DocumentChunkRepository documentChunkRepository;
    private static final Logger log = LoggerFactory.getLogger(DocumentProcessingService.class);

    public DocumentProcessingService(DocumentRepository documentRepository, 
        DocumentTextExtractionService extractionService, 
        DocumentIndexService indexService,
        SearchIndexService searchIndexService,
        EmbeddingService embeddingService, DocumentChunkRepository documentChunkRepository) {

        this.documentRepository = documentRepository;
        this.extractionService = extractionService;
        this.indexService = indexService;
        this.searchIndexService = searchIndexService;
        this.embeddingService = embeddingService;
        this.documentChunkRepository = documentChunkRepository;
    }
    
    @Transactional
    public void processDocument(Long documentId, String filePath, String fileType) throws JsonProcessingException {
        Document document = documentRepository.findById(documentId).orElseThrow();
        document.setProcessingStatus(DocumentProcessingStatus.PROCESSING);
        document.setProcessingError(null);
        document.setProcessingMessage("Extracting text from document.");
        document.setProcessingStartedAt(java.time.LocalDateTime.now());
        document.setProcessingCompletedAt(null);
        documentRepository.save(document);

        try {
            log.info("Processing document {} of type {}", documentId, fileType);

            String extractedText = extractionService.extractText(filePath);
            log.debug("Extracted text length : {}", extractedText.length());

            document.setExtractedText(extractedText);
            document.setProcessingMessage("Generating embeddings for document chunks.");
            documentRepository.save(document);

            List<String> chunks = TextChunker.chunkText(extractedText, 500);
            log.info("Total chunks created : {}", chunks.size());

            for (String chunk : chunks) {
                List<Double> embedding = embeddingService.generateEmbedding(chunk);
                String vectorJson = new ObjectMapper().writeValueAsString(embedding);

                DocumentChunk documentChunk = new DocumentChunk();
                documentChunk.setDocument(document);
                documentChunk.setChunkText(chunk);
                documentChunk.setEmbeddingVector(vectorJson);

                documentChunkRepository.save(documentChunk);
            }

            document.setProcessingMessage("Indexing document for search.");
            documentRepository.save(document);

            DocumentIndex index = indexService.buildIndex(document);
            searchIndexService.indexDocument(index);

            document.setProcessingStatus(DocumentProcessingStatus.READY);
            document.setProcessingError(null);
            document.setProcessingMessage("Ready for preview, search, and chat.");
            document.setProcessingCompletedAt(java.time.LocalDateTime.now());
            documentRepository.save(document);

            log.info("Index prepared for document {}", index.getDocumentId());
        } catch (Exception exception) {
            log.error("Document processing failed for {}", documentId, exception);
            document.setProcessingStatus(DocumentProcessingStatus.FAILED);
            document.setProcessingError(exception.getMessage());
            document.setProcessingMessage("Processing failed.");
            document.setProcessingCompletedAt(java.time.LocalDateTime.now());
            documentRepository.save(document);
        }
    }
}
