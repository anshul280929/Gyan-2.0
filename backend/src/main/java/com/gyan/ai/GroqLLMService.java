package com.gyan.ai;
import org.springframework.http.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class GroqLLMService implements LLMService {
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.api.url}")
    private String apiUrl;

    @Override
    public String generateAnswer(String question, String context) {
        Map<String, Object> body = new HashMap<>();

        body.put("model", "llama-3.1-8b-instant");

        List<Map<String, String>> messages = new ArrayList<>();

        Map<String, String> system = new HashMap<>();
        system.put("role", "system");
        system.put("content", "Answer the question using only the provided context");

        Map<String, String> user = new HashMap<>();
        user.put("role", "user");
        user.put("content", "Context:\n" + context + "\n\nQuestion:\n" + question);

        messages.add(system);
        messages.add(user);

        body.put("messages", messages);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, request, Map.class);

        Map choice =     ((List<Map>) response.getBody().get("choices")).get(0);
        Map message = (Map) choice.get("message");

        return (String) message.get("content");
    }

}
