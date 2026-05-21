package com.gyan.search;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;


@Document(indexName = "documents")
public class DocumentIndex {
    @Id
    private Long documentId;
    private String title;
    private String content;
    private String ownerEmail;

    public DocumentIndex(Long documentId, String title, String content, String ownerEmail) {
        this.documentId = documentId;
        this.title = title;
        this.content = content;
        this.ownerEmail = ownerEmail;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    
}
