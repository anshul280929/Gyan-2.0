package com.gyan.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.gyan.dto.ChatCreateRequestDTO;
import com.gyan.dto.ChatResponseDTO;
import com.gyan.dto.NameUpdateRequestDTO;
import com.gyan.entity.Chat;
import com.gyan.entity.User;
import com.gyan.exception.BadRequestException;
import com.gyan.model.Role;
import com.gyan.repository.ChatRepository;
import com.gyan.repository.DocumentChunkRepository;
import com.gyan.repository.DocumentRepository;
import com.gyan.search.SearchIndexService;
import com.gyan.storage.StorageService;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {
    @Mock
    private ChatRepository chatRepository;

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private DocumentChunkRepository documentChunkRepository;

    @Mock
    private SearchIndexService searchIndexService;

    @Mock
    private StorageService storageService;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private ChatService chatService;

    @Test
    void createChatRejectsWhenLimitReached() {
        User user = new User("user@example.com", "hashed", Role.USER);
        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(chatRepository.countByUser(user)).thenReturn(5L);

        ChatCreateRequestDTO request = new ChatCreateRequestDTO();
        request.setName("Invoices");

        assertThrows(BadRequestException.class, () -> chatService.createChat(request));
        verify(chatRepository, never()).save(any(Chat.class));
    }

    @Test
    void renameChatUpdatesNameAndTimestamp() {
        User user = new User("user@example.com", "hashed", Role.USER);
        Chat chat = new Chat();
        chat.setId(9L);
        chat.setName("Old name");
        chat.setCreatedAt(LocalDateTime.now().minusDays(1));
        chat.setUpdatedAt(LocalDateTime.now().minusHours(2));
        chat.setUser(user);

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(chatRepository.findByIdAndUser(9L, user)).thenReturn(Optional.of(chat));
        when(chatRepository.save(chat)).thenReturn(chat);
        when(documentRepository.countByChat(chat)).thenReturn(0L);

        NameUpdateRequestDTO request = new NameUpdateRequestDTO();
        request.setName("Renamed workspace");

        ChatResponseDTO response = chatService.renameChat(9L, request);

        assertEquals("Renamed workspace", response.getName());
        verify(chatRepository).save(chat);
        verify(auditLogService).log("chat.rename", user.getEmail(), "SUCCESS", "chatId=9 name=Renamed workspace");
    }
}
