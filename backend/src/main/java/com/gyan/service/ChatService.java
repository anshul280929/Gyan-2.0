package com.gyan.service;

import java.time.LocalDateTime;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gyan.dto.ChatCreateRequestDTO;
import com.gyan.dto.ChatResponseDTO;
import com.gyan.dto.NameUpdateRequestDTO;
import com.gyan.entity.Chat;
import com.gyan.entity.Document;
import com.gyan.entity.User;
import com.gyan.exception.BadRequestException;
import com.gyan.exception.NotFoundException;
import com.gyan.repository.ChatMessageRepository;
import com.gyan.repository.ChatRepository;
import com.gyan.repository.DocumentChunkRepository;
import com.gyan.repository.DocumentRepository;
import com.gyan.search.SearchIndexService;
import com.gyan.storage.StorageService;

@Service
public class ChatService {
    private static final long MAX_CHATS_PER_USER = 5;

    private final ChatRepository chatRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository documentChunkRepository;
    private final SearchIndexService searchIndexService;
    private final StorageService storageService;
    private final CurrentUserService currentUserService;
    private final AuditLogService auditLogService;

    public ChatService(
        ChatRepository chatRepository,
        ChatMessageRepository chatMessageRepository,
        DocumentRepository documentRepository,
        DocumentChunkRepository documentChunkRepository,
        SearchIndexService searchIndexService,
        StorageService storageService,
        CurrentUserService currentUserService,
        AuditLogService auditLogService
    ) {
        this.chatRepository = chatRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.documentRepository = documentRepository;
        this.documentChunkRepository = documentChunkRepository;
        this.searchIndexService = searchIndexService;
        this.storageService = storageService;
        this.currentUserService = currentUserService;
        this.auditLogService = auditLogService;
    }

    public List<ChatResponseDTO> getCurrentUserChats() {
        User user = currentUserService.getCurrentUser();

        return chatRepository.findByUserOrderByUpdatedAtDesc(user)
            .stream()
            .map(this::mapToDTO)
            .toList();
    }

    public ChatResponseDTO createChat(ChatCreateRequestDTO request) {
        User user = currentUserService.getCurrentUser();

        if (chatRepository.countByUser(user) >= MAX_CHATS_PER_USER) {
            throw new BadRequestException("You can create at most 5 chats.");
        }

        LocalDateTime now = LocalDateTime.now();

        Chat chat = new Chat();
        chat.setName(request.getName().trim());
        chat.setCreatedAt(now);
        chat.setUpdatedAt(now);
        chat.setUser(user);

        Chat savedChat = chatRepository.save(chat);
        auditLogService.log("chat.create", user.getEmail(), "SUCCESS", "chatId=" + savedChat.getId() + " name=" + savedChat.getName());
        return mapToDTO(savedChat);
    }

    public Chat getOwnedChat(Long chatId) {
        User user = currentUserService.getCurrentUser();
        return chatRepository.findByIdAndUser(chatId, user)
            .orElseThrow(() -> new NotFoundException("Chat not found"));
    }

    public ChatResponseDTO getChat(Long chatId) {
        return mapToDTO(getOwnedChat(chatId));
    }

    public void touch(Chat chat) {
        chat.setUpdatedAt(LocalDateTime.now());
        chatRepository.save(chat);
    }

    @Transactional
    public ChatResponseDTO renameChat(Long chatId, NameUpdateRequestDTO request) {
        Chat chat = getOwnedChat(chatId);
        chat.setName(request.getName().trim());
        chat.setUpdatedAt(LocalDateTime.now());
        Chat savedChat = chatRepository.save(chat);
        auditLogService.log("chat.rename", chat.getUser().getEmail(), "SUCCESS", "chatId=" + savedChat.getId() + " name=" + savedChat.getName());
        return mapToDTO(savedChat);
    }

    @Transactional
    public void deleteChat(Long chatId) {
        Chat chat = getOwnedChat(chatId);
        List<Document> documents = documentRepository.findAllByChat(chat);

        chatMessageRepository.deleteByChat(chat);

        for (Document document : documents) {
            searchIndexService.deleteDocument(document.getId());
            deleteDocumentFile(document);
        }

        if (!documents.isEmpty()) {
            documentChunkRepository.deleteByDocumentIn(documents);
            documentRepository.deleteByChat(chat);
        }

        chatRepository.delete(chat);
        auditLogService.log("chat.delete", chat.getUser().getEmail(), "SUCCESS", "chatId=" + chat.getId() + " name=" + chat.getName());
    }

    private void deleteDocumentFile(Document document) {
        if (document.getStoredFileName() != null && !document.getStoredFileName().isBlank()) {
            storageService.delete(document.getStoredFileName());
            return;
        }

        if (document.getFilePath() == null || document.getFilePath().isBlank()) {
            return;
        }

        try {
            Path filePath = Paths.get(document.getFilePath()).toAbsolutePath().normalize();
            Files.deleteIfExists(filePath);
        } catch (Exception exception) {
            throw new RuntimeException("Unable to delete file");
        }
    }

    private ChatResponseDTO mapToDTO(Chat chat) {
        return new ChatResponseDTO(
            chat.getId(),
            chat.getName(),
            chat.getCreatedAt(),
            chat.getUpdatedAt(),
            documentRepository.countByChat(chat)
        );
    }
}
