package com.gyan.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gyan.dto.DocumentResponseDTO;
import com.gyan.service.SemanticSearchService;

@RestController
@RequestMapping("/documents/chats")
public class SemanticSearchController {
    private final SemanticSearchService semanticSearchService;    

    public SemanticSearchController(SemanticSearchService semanticSearchService) {
        this.semanticSearchService = semanticSearchService;
    }   

    @GetMapping("/{chatId}/semantic-search")
    public List<DocumentResponseDTO> sematicSearch(@PathVariable Long chatId, @RequestParam String q) throws Exception {
        return semanticSearchService.semanticSearch(chatId, q);
    }
 
}
