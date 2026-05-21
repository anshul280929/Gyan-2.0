package com.gyan.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.gyan.event.DocumentUploadedEvent;
import com.gyan.service.DocumentProcessingService;

@Service
public class DocumentEventConsumer {
    private final DocumentProcessingService processingService;

    public DocumentEventConsumer(DocumentProcessingService processingService) {
        this.processingService = processingService;
    }

    @KafkaListener(topics = "document-uploaded", groupId = "document-workers")
    public void consume(DocumentUploadedEvent event) throws JsonProcessingException {
        processingService.processDocument(
            event.getDocumentId(),
            event.getFilePath(),
            event.getFileType()
        );
    }
}
