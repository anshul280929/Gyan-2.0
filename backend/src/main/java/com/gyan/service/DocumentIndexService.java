package com.gyan.service;

import org.springframework.stereotype.Service;

import com.gyan.entity.Document;
import com.gyan.search.DocumentIndex;
import com.gyan.util.TextNormalizer;

@Service
public class DocumentIndexService {
    public DocumentIndex buildIndex(Document document) {
        String normalizedText = TextNormalizer.normalize(document.getExtractedText());

        return new DocumentIndex(
            document.getId(),
            document.getFilename(), 
            normalizedText, 
            document.getUser().getEmail()
        );
    }
}
