package com.gyan.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class DocumentChunk {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private Document document;

    @Column(columnDefinition = "TEXT")
    private String chunkText;

    @Column(columnDefinition = "TEXT")
    private String embeddingVector;


     public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public String getChunkText() {
        return chunkText;
    }

    public void setChunkText(String chunkText) {
        this.chunkText = chunkText;
    }

    public String getEmbeddingVector() {
        return embeddingVector;
    }

    public void setEmbeddingVector(String embeddingVector) {
        this.embeddingVector = embeddingVector;
    }
}
