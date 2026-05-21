package com.gyan.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gyan.dto.ChatMessageResponseDTO;
import com.gyan.entity.Chat;
import com.gyan.entity.ChatMessage;
import com.gyan.model.ChatMessageRole;
import com.gyan.repository.ChatMessageRepository;

@Service
public class ChatMessageService {
    private final ChatMessageRepository chatMessageRepository;
    private final ChatService chatService;

    public ChatMessageService(ChatMessageRepository chatMessageRepository, ChatService chatService) {
        this.chatMessageRepository = chatMessageRepository;
        this.chatService = chatService;
    }

    public List<ChatMessageResponseDTO> getMessages(Long chatId) {
        Chat chat = chatService.getOwnedChat(chatId);
        return chatMessageRepository.findByChatOrderByCreatedAtAsc(chat)
            .stream()
            .map(message -> new ChatMessageResponseDTO(
                message.getId(),
                message.getRole().name(),
                message.getContent(),
                message.getCreatedAt()
            ))
            .toList();
    }

    @Transactional
    public void saveExchange(Chat chat, String question, String answer) {
        chatMessageRepository.save(buildMessage(chat, ChatMessageRole.USER, question));
        chatMessageRepository.save(buildMessage(chat, ChatMessageRole.ASSISTANT, answer));
    }

    private ChatMessage buildMessage(Chat chat, ChatMessageRole role, String content) {
        ChatMessage message = new ChatMessage();
        message.setChat(chat);
        message.setRole(role);
        message.setContent(content);
        message.setCreatedAt(LocalDateTime.now());
        return message;
    }
}
