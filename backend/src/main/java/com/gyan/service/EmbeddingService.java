package com.gyan.service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class EmbeddingService {
    
    private final RestTemplate restTemplate = new RestTemplate();
    private static final Logger log = LoggerFactory.getLogger(EmbeddingService.class);

    @Value("${embedding.service.url:http://localhost:8000}")
    private String embeddingServiceUrl;

    public List<Double> generateEmbedding(String text) {
        log.debug("calling embedding service for text size: " + text.length());
        
        Map<String, String> request = new HashMap<>();
        request.put("text", text);

        ResponseEntity<Map> response = restTemplate.postForEntity(embeddingServiceUrl + "/embedding",
                request, Map.class);
        
        List<Double> embedding = (List<Double>) response.getBody().get("embedding");

        return embedding;
    }

}
