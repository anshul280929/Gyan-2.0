package com.gyan.event;


public class DocumentUploadedEvent {
    private Long documentId;
    private String filePath;
    private String fileType;
    private Long userId;

    public DocumentUploadedEvent() {}
    public DocumentUploadedEvent(Long documentId, String filePath, String fileType, Long userId) {
        this.documentId = documentId;
        this.filePath = filePath;
        this.fileType = fileType;
        this.userId = userId;
    }
    public Long getDocumentId() {
        return documentId;
    }
    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }
    public String getFilePath() {
        return filePath;
    }
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    public String getFileType() {
        return fileType;
    }
    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    
}