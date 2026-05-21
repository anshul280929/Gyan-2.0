package com.gyan.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.gyan.event.DocumentUploadedEvent;

@Service
public class DocumentEventProducer {
    private final KafkaTemplate<String, DocumentUploadedEvent> kafkaTemplate;

    public DocumentEventProducer(KafkaTemplate<String, DocumentUploadedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishDocumentUploaded(DocumentUploadedEvent event) {
        kafkaTemplate.send("document-uploaded", event);
    }

}
