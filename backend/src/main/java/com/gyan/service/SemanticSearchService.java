package com.gyan.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gyan.entity.Chat;
import com.gyan.entity.Document;
import com.gyan.entity.DocumentChunk;
import com.gyan.repository.DocumentChunkRepository;
import com.gyan.repository.DocumentRepository;
import com.gyan.util.VectorSimilarityUtil;
import com.gyan.dto.DocumentResponseDTO;

@Service
public class SemanticSearchService {
    private final DocumentChunkRepository documentChunkRepository;
    private final DocumentRepository documentRepository;
    private final EmbeddingService embeddingService;
    private final ChatService chatService;
    private static final Logger log = LoggerFactory.getLogger(SemanticSearchService.class);

    public SemanticSearchService(
        DocumentChunkRepository documentChunkRepository,
        DocumentRepository documentRepository,
        EmbeddingService embeddingService,
        ChatService chatService
    ) {
        this.documentChunkRepository = documentChunkRepository;
        this.documentRepository = documentRepository;
        this.embeddingService = embeddingService;
        this.chatService = chatService;
    }

    public List<DocumentResponseDTO> semanticSearch(Long chatId, String query) throws Exception {
        log.info("Semantic search started for query : " + query);
        List<Double> queryEmbedding = embeddingService.generateEmbedding(query);
        Chat chat = chatService.getOwnedChat(chatId);
        List<Document> documents = documentRepository.findByChat(chat, Pageable.unpaged()).getContent();
        List<DocumentChunk> chunks = new ArrayList<>();

        for (Document document : documents) {
            chunks.addAll(documentChunkRepository.findByDocument(document));
        }

        List<Map.Entry<DocumentChunk, Double>> scores = new ArrayList<>();

        ObjectMapper mapper = new ObjectMapper();

        for(DocumentChunk chunk : chunks) {
            if(chunk.getEmbeddingVector() == null)   continue;

            List<Double> chunkEmbedding = mapper.readValue(chunk.getEmbeddingVector(), List.class);;

            double similarity = VectorSimilarityUtil.cosineSimilarity(queryEmbedding, chunkEmbedding);
            scores.add(Map.entry(chunk, similarity));
        }

        // Sort by similarity descending
        List<Document> result =
                scores.stream()
                        .sorted((a, b) ->
                                Double.compare(
                                        b.getValue(),
                                        a.getValue()))
                        .limit(5)
                        .map(entry ->
                                entry.getKey().getDocument())
                        .distinct()
                        .toList();

        log.info("Top chunks retrieved : " + result.size());

        return result.stream().map(this::mapToDto).toList();
    }

    public List<DocumentChunk> findRelevantChunks(String query) throws Exception {
        List<DocumentChunk> chunks =
                documentChunkRepository.findAll();

        return rankRelevantChunks(query, chunks);
    }

    public List<DocumentChunk> findRelevantChunks(Long chatId, String query) throws Exception {
        Chat chat = chatService.getOwnedChat(chatId);
        List<Document> documents = documentRepository.findByChat(chat, Pageable.unpaged()).getContent();
        List<DocumentChunk> chunks = new ArrayList<>();

        for (Document document : documents) {
            chunks.addAll(documentChunkRepository.findByDocument(document));
        }

        return rankRelevantChunks(query, chunks);
    }

    private List<DocumentChunk> rankRelevantChunks(String query, List<DocumentChunk> chunks) throws Exception {
        List<Double> queryEmbedding = embeddingService.generateEmbedding(query);

        List<Map.Entry<DocumentChunk, Double>> scores = calculateScores(queryEmbedding, chunks);

        return scores.stream()
            .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
            .limit(5)
            .map(Map.Entry::getKey)
            .toList();
    }

    private List<Map.Entry<DocumentChunk, Double>> calculateScores(
            List<Double> queryEmbedding,
            List<DocumentChunk> chunks) throws Exception {

        List<Map.Entry<DocumentChunk, Double>> scores = new ArrayList<>();

        ObjectMapper mapper = new ObjectMapper();

        for (DocumentChunk chunk : chunks) {

            if (chunk.getEmbeddingVector() == null) continue;

            List<Double> chunkEmbedding =
                    mapper.readValue(
                            chunk.getEmbeddingVector(),
                            List.class
                    );

            double similarity =
                    VectorSimilarityUtil.cosineSimilarity(
                            queryEmbedding,
                            chunkEmbedding
                    );

            scores.add(Map.entry(chunk, similarity));
        }

        return scores;
    }

    private DocumentResponseDTO mapToDto(Document document) {
        DocumentResponseDTO dto = new DocumentResponseDTO();
        dto.setId(document.getId());
        dto.setFileName(document.getFilename());
        dto.setFileType(document.getFileType());
        dto.setFileSize(document.getFileSize());
        dto.setFilePath(document.getFilePath());
        dto.setUploadedAt(document.getUploadedAt());
        dto.setOwnerEmail(document.getUser().getEmail());
        dto.setChatId(document.getChat() != null ? document.getChat().getId() : null);
        return dto;
    }
}
