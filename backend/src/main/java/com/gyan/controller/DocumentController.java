package com.gyan.controller;

import java.io.IOException;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.gyan.dto.DocumentResponseDTO;
import com.gyan.dto.NameUpdateRequestDTO;
import com.gyan.service.DocumentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/documents")
public class DocumentController {
    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping("/chats/{chatId}/upload")
    public DocumentResponseDTO uploadDocumentToChat(
        @PathVariable Long chatId,
        @RequestParam("file") MultipartFile file
    ) throws IOException {
        return documentService.uploadFile(chatId, file);
    }

    @PostMapping("/upload")
    public DocumentResponseDTO uploadDocument(@RequestParam("file") MultipartFile file) throws IOException {
        throw new UnsupportedOperationException("Use /documents/chats/{chatId}/upload");
    }

    @GetMapping("/chats/{chatId}/{id}/download")
    public ResponseEntity<Resource> downloadDocumentFromChat(@PathVariable Long chatId, @PathVariable Long id) {
        Resource resource = documentService.downloadDocument(chatId, id);
        DocumentResponseDTO document = documentService.getDocumentByChatAndId(chatId, id);

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.getFileName() + "\"")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(resource);
    }

    @GetMapping("/chats/{chatId}/{id}/preview")
    public ResponseEntity<Resource> previewDocumentFromChat(@PathVariable Long chatId, @PathVariable Long id) {
        Resource resource = documentService.downloadDocument(chatId, id);
        MediaType mediaType = documentService.getPreviewMediaType(chatId, id);
        DocumentResponseDTO document = documentService.getDocumentByChatAndId(chatId, id);

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + document.getFileName() + "\"")
            .contentType(mediaType)
            .body(resource);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long id) {
        Resource resource = documentService.downloadDocument(id);
        DocumentResponseDTO document = documentService.getDocumentById(id);

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.getFileName() + "\"")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(resource);
    }

    @GetMapping("/chats/{chatId}")
    public Page<DocumentResponseDTO> getDocumentsForChat(@PathVariable Long chatId, Pageable pageable) {
        return documentService.getDocumentsByChat(chatId, pageable);
    }

    @GetMapping
    public Page<DocumentResponseDTO> getDocuments(Pageable pageable) {
        return documentService.getDocuments(pageable);
    }

    @GetMapping("/chats/{chatId}/{id}")
    public DocumentResponseDTO getDocumentForChat(@PathVariable Long chatId, @PathVariable Long id) {
        return documentService.getDocumentByChatAndId(chatId, id);
    }

    @DeleteMapping("/chats/{chatId}/{id}")
    public ResponseEntity<Void> deleteDocumentForChat(@PathVariable Long chatId, @PathVariable Long id) {
        documentService.deleteDocumentByChatAndId(chatId, id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/chats/{chatId}/{id}")
    public DocumentResponseDTO renameDocumentForChat(
        @PathVariable Long chatId,
        @PathVariable Long id,
        @Valid @RequestBody NameUpdateRequestDTO request
    ) {
        return documentService.renameDocument(chatId, id, request);
    }

    @GetMapping("/{id}")
    public DocumentResponseDTO getDocument(@PathVariable Long id) {
        return documentService.getDocumentById(id);
    }

}
