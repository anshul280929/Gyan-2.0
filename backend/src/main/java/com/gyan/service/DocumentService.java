package com.gyan.service;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.gyan.dto.DocumentResponseDTO;
import com.gyan.dto.NameUpdateRequestDTO;
import com.gyan.entity.Chat;
import com.gyan.entity.Document;
import com.gyan.entity.User;
import com.gyan.event.DocumentUploadedEvent;
import com.gyan.exception.ForbiddenException;
import com.gyan.exception.NotFoundException;
import com.gyan.model.DocumentProcessingStatus;
import com.gyan.producer.DocumentEventProducer;
import com.gyan.repository.DocumentRepository;
import com.gyan.repository.DocumentChunkRepository;
import com.gyan.search.SearchIndexService;
import com.gyan.storage.StorageService;
import com.gyan.util.FileValidator;

@Service
public class DocumentService {
    private final DocumentRepository documentRepository;
    private final CurrentUserService currentUserService;
    private final ChatService chatService;
    private final DocumentChunkRepository documentChunkRepository;
    private final SearchIndexService searchIndexService;
    private final StorageService storageService;
    private final FileValidator fileValidator;
    private final DocumentEventProducer documentEventProducer;
    private final AuditLogService auditLogService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public DocumentService(
            DocumentRepository documentRepository 
            ,StorageService storageService
            ,CurrentUserService currentUserService
            ,ChatService chatService
            ,DocumentChunkRepository documentChunkRepository
            ,SearchIndexService searchIndexService
            ,FileValidator fileValidator
            ,DocumentEventProducer documentEventProducer
            ,AuditLogService auditLogService) {

        this.documentRepository = documentRepository;
        this.storageService = storageService;
        this.currentUserService = currentUserService;
        this.chatService = chatService;
        this.documentChunkRepository = documentChunkRepository;
        this.searchIndexService = searchIndexService;
        this.fileValidator = fileValidator;
        this.documentEventProducer = documentEventProducer; 
        this.auditLogService = auditLogService;
    }

    public DocumentResponseDTO uploadFile(Long chatId, MultipartFile file) throws IOException {

        fileValidator.validate(file);

        User user = currentUserService.getCurrentUser();
        Chat chat = chatService.getOwnedChat(chatId);
                
        String storedFileName = storageService.store(file);

        Document document = new Document();

        document.setFilename(file.getOriginalFilename());
        document.setStoredFileName(storedFileName);
        document.setFileType(file.getContentType());
        document.setFileSize(file.getSize());
        document.setFilePath(storedFileName);
        document.setFilePath(uploadDir + "/" + storedFileName);
        document.setUploadedAt(LocalDateTime.now());
        document.setProcessingStatus(DocumentProcessingStatus.UPLOADED);
        document.setProcessingError(null);
        document.setProcessingMessage("Queued for text extraction and indexing.");
        document.setProcessingStartedAt(null);
        document.setProcessingCompletedAt(null);
        document.setUser(user);
        document.setChat(chat);

        Document saved = documentRepository.save(document);
        chatService.touch(chat);

        // documentProcessingService.processDocument(saved.getFilePath());

        DocumentUploadedEvent event = new DocumentUploadedEvent(
            document.getId(),
            document.getFilePath(),
            document.getFileType(),
            user.getId()
        );

        documentEventProducer.publishDocumentUploaded(event);
        auditLogService.log("document.upload", user.getEmail(), "SUCCESS", "documentId=" + saved.getId() + " chatId=" + chatId + " file=" + saved.getFilename());

        return mapToDTO(saved);
    }

    public Resource downloadDocument(Long id) {
        return downloadDocument(null, id);
    }

    public Document getOwnedDocument(Long id) {
        Document document = documentRepository
            .findById(id)
            .orElseThrow(() -> new NotFoundException("Document not found"));

        User user = currentUserService.getCurrentUser();

        if (!document.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("Unauthorized access");
        }

        return document;
    }

    public Resource downloadDocument(Long chatId, Long id) {
        Document document = getOwnedDocument(id);

        if (chatId != null) {
            if (document.getChat() == null || !document.getChat().getId().equals(chatId)) {
                throw new ForbiddenException("Document does not belong to this chat");
            }
        }
        
        auditLogService.log("document.download", document.getUser().getEmail(), "SUCCESS", "documentId=" + document.getId() + " chatId=" + (document.getChat() != null ? document.getChat().getId() : "none"));
        return resolveDocumentResource(document);
    }

    public MediaType getPreviewMediaType(Long chatId, Long id) {
        Document document = getOwnedDocument(id);

        if (chatId != null && (document.getChat() == null || !document.getChat().getId().equals(chatId))) {
            throw new ForbiddenException("Document does not belong to this chat");
        }

        auditLogService.log("document.preview", document.getUser().getEmail(), "SUCCESS", "documentId=" + document.getId() + " chatId=" + (document.getChat() != null ? document.getChat().getId() : "none"));

        try {
            return document.getFileType() != null ? MediaType.parseMediaType(document.getFileType()) : MediaType.APPLICATION_OCTET_STREAM;
        } catch (Exception exception) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    private DocumentResponseDTO mapToDTO(Document document) {

        DocumentResponseDTO dto = new DocumentResponseDTO();

        dto.setId(document.getId());
        dto.setFileName(document.getFilename());
        dto.setFileType(document.getFileType());
        dto.setFileSize(document.getFileSize());
        dto.setFilePath(document.getFilePath());
        dto.setUploadedAt(document.getUploadedAt());
        dto.setOwnerEmail(document.getUser().getEmail());
        dto.setChatId(document.getChat() != null ? document.getChat().getId() : null);
        dto.setProcessingStatus(document.getProcessingStatus() != null ? document.getProcessingStatus().name() : null);
        dto.setProcessingError(document.getProcessingError());
        dto.setProcessingMessage(document.getProcessingMessage());
        dto.setProcessingStartedAt(document.getProcessingStartedAt());
        dto.setProcessingCompletedAt(document.getProcessingCompletedAt());

        return dto;
    }

    public Page<DocumentResponseDTO> getDocuments(Pageable pageable) {
        User user = currentUserService.getCurrentUser();
        
        Page<Document> documents = documentRepository.findByUser(user, pageable);

        return documents.map(this::mapToDTO);
    }

    public Page<DocumentResponseDTO> getDocumentsByChat(Long chatId, Pageable pageable) {
        Chat chat = chatService.getOwnedChat(chatId);

        return documentRepository.findByChat(chat, pageable)
            .map(this::mapToDTO);
    }


    public DocumentResponseDTO getDocumentById(Long id) {

        Document document = documentRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException("Document not found"));
        return mapToDTO(getOwnedDocument(document.getId()));
    }

    public DocumentResponseDTO getDocumentByChatAndId(Long chatId, Long id) {
        Document document = getOwnedDocument(id);

        if (document.getChat() == null || !document.getChat().getId().equals(chatId)) {
            throw new ForbiddenException("Document does not belong to this chat");
        }

        return mapToDTO(document);
    }

    @Transactional
    public DocumentResponseDTO renameDocument(Long chatId, Long id, NameUpdateRequestDTO request) {
        Document document = getOwnedDocument(id);

        if (document.getChat() == null || !document.getChat().getId().equals(chatId)) {
            throw new ForbiddenException("Document does not belong to this chat");
        }

        document.setFilename(request.getName().trim());
        Document savedDocument = documentRepository.save(document);
        chatService.touch(document.getChat());
        auditLogService.log("document.rename", document.getUser().getEmail(), "SUCCESS", "documentId=" + savedDocument.getId() + " name=" + savedDocument.getFilename());
        return mapToDTO(savedDocument);
    }

    @Transactional
    public void deleteDocumentByChatAndId(Long chatId, Long id) {
        Document document = getOwnedDocument(id);

        if (document.getChat() == null || !document.getChat().getId().equals(chatId)) {
            throw new ForbiddenException("Document does not belong to this chat");
        }

        deleteDocumentResources(document);
        chatService.touch(document.getChat());
        auditLogService.log("document.delete", document.getUser().getEmail(), "SUCCESS", "documentId=" + document.getId() + " chatId=" + chatId);
    }

    @Transactional
    public void deleteDocumentResources(Document document) {
        documentChunkRepository.deleteByDocument(document);
        searchIndexService.deleteDocument(document.getId());

        if (document.getStoredFileName() != null && !document.getStoredFileName().isBlank()) {
            storageService.delete(document.getStoredFileName());
        } else if (document.getFilePath() != null && !document.getFilePath().isBlank()) {
            try {
                Path filePath = Paths.get(document.getFilePath()).toAbsolutePath().normalize();
                java.nio.file.Files.deleteIfExists(filePath);
            } catch (IOException exception) {
                throw new RuntimeException("Unable to delete file");
            }
        }

        documentRepository.delete(document);
    }

    private Resource resolveDocumentResource(Document document) {
        if (document.getStoredFileName() != null && !document.getStoredFileName().isBlank()) {
            return storageService.load(document.getStoredFileName());
        }

        if (document.getFilePath() != null && !document.getFilePath().isBlank()) {
            try {
                Path filePath = Paths.get(document.getFilePath()).toAbsolutePath().normalize();
                Resource resource = new UrlResource(filePath.toUri());

                if (resource.exists() && resource.isReadable()) {
                    return resource;
                }
            } catch (Exception exception) {
                throw new NotFoundException("File not found");
            }
        }

        throw new NotFoundException("File not found");
    }
}
