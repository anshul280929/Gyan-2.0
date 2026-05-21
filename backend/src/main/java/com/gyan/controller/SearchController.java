package com.gyan.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gyan.search.DocumentIndex;
import com.gyan.service.DocumentSearchService;

@RestController
@RequestMapping("/documents")
public class SearchController {
    private final DocumentSearchService searchService;

    public SearchController(DocumentSearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/search")
    public List<DocumentIndex> search(@RequestParam String q, Authentication authentication) {
        String userEmail = authentication.getName();
        return searchService.search(q, userEmail);
    }
}
