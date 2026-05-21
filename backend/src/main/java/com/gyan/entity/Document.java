package com.gyan.entity;

import java.time.LocalDateTime;

import com.gyan.model.DocumentProcessingStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public void setId(Long id) {
        this.id = id;
    }

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "chat_id")
    private Chat chat;

    private String filename;
    private String storedFileName;
    private String fileType;
    private Long fileSize;
    private String filePath;
    private LocalDateTime uploadedAt;

    @Enumerated(EnumType.STRING)
    private DocumentProcessingStatus processingStatus;

    @Column(columnDefinition = "TEXT")
    private String processingError;

    @Column(columnDefinition = "TEXT")
    private String processingMessage;

    private LocalDateTime processingStartedAt;
    private LocalDateTime processingCompletedAt;

    @Column(columnDefinition = "TEXT")
    private String extractedText;

    @Column(columnDefinition = "TEXT")
    private String embeddingVector;
    

    public Long getId() {
        return id;
    }

    public String getFilename() {
        return filename;
    }

    public String getFileType() {
        return fileType;
    }

      public String getFilePath() {
        return filePath;
    }

    public String getStoredFileName() {
        return storedFileName;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public DocumentProcessingStatus getProcessingStatus() {
        return processingStatus;
    }

    public String getProcessingError() {
        return processingError;
    }

    public String getProcessingMessage() {
        return processingMessage;
    }

    public LocalDateTime getProcessingStartedAt() {
        return processingStartedAt;
    }

    public LocalDateTime getProcessingCompletedAt() {
        return processingCompletedAt;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

      public void setStoredFileName(String storedFileName) {
        this.storedFileName = storedFileName;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public void setProcessingStatus(DocumentProcessingStatus processingStatus) {
        this.processingStatus = processingStatus;
    }

    public void setProcessingError(String processingError) {
        this.processingError = processingError;
    }

    public void setProcessingMessage(String processingMessage) {
        this.processingMessage = processingMessage;
    }

    public void setProcessingStartedAt(LocalDateTime processingStartedAt) {
        this.processingStartedAt = processingStartedAt;
    }

    public void setProcessingCompletedAt(LocalDateTime processingCompletedAt) {
        this.processingCompletedAt = processingCompletedAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Chat getChat() {
        return chat;
    }

    public void setChat(Chat chat) {
        this.chat = chat;
    }
    
    public String getExtractedText() {
        return extractedText;
    }

    public void setExtractedText(String extractedText) {
        this.extractedText = extractedText;
    }
    
    public String getEmbeddingVector() {
        return embeddingVector;
    }

    public void setEmbeddingVector(String embeddingVector) {
        this.embeddingVector = embeddingVector;
    }

}
