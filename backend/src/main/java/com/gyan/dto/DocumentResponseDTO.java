package com.gyan.dto;

import java.time.LocalDateTime;

public class DocumentResponseDTO {
    private Long id;
    private String fileName;
    private String fileType;
    private String filePath;
    private Long fileSize;
    private LocalDateTime uploadedAt;
    private String ownerEmail;
    private Long chatId;
    private String processingStatus;
    private String processingError;
    private String processingMessage;
    private LocalDateTime processingStartedAt;
    private LocalDateTime processingCompletedAt;


    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
    public String getFileName() {
        return fileName;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    public String getFileType() {
        return fileType;
    }
    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
    public String getFilePath() {
        return filePath;
    }
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    public Long getFileSize() {
        return fileSize;
    }
    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }
    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }
    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
    public String getOwnerEmail() {
        return ownerEmail;
    }
    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }
    public Long getChatId() {
        return chatId;
    }
    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }
    public String getProcessingStatus() {
        return processingStatus;
    }
    public void setProcessingStatus(String processingStatus) {
        this.processingStatus = processingStatus;
    }
    public String getProcessingError() {
        return processingError;
    }
    public void setProcessingError(String processingError) {
        this.processingError = processingError;
    }
    public String getProcessingMessage() {
        return processingMessage;
    }
    public void setProcessingMessage(String processingMessage) {
        this.processingMessage = processingMessage;
    }
    public LocalDateTime getProcessingStartedAt() {
        return processingStartedAt;
    }
    public void setProcessingStartedAt(LocalDateTime processingStartedAt) {
        this.processingStartedAt = processingStartedAt;
    }
    public LocalDateTime getProcessingCompletedAt() {
        return processingCompletedAt;
    }
    public void setProcessingCompletedAt(LocalDateTime processingCompletedAt) {
        this.processingCompletedAt = processingCompletedAt;
    }

}
