package com.gyan.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

import com.gyan.dto.ChatCreateRequestDTO;
import com.gyan.dto.NameUpdateRequestDTO;
import com.gyan.dto.ChatResponseDTO;
import com.gyan.service.ChatService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/chats")
public class ChatController {
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping
    public List<ChatResponseDTO> getChats() {
        return chatService.getCurrentUserChats();
    }

    @PostMapping
    public ChatResponseDTO createChat(@Valid @RequestBody ChatCreateRequestDTO request) {
        return chatService.createChat(request);
    }

    @GetMapping("/{chatId}")
    public ChatResponseDTO getChat(@PathVariable Long chatId) {
        return chatService.getChat(chatId);
    }

    @PatchMapping("/{chatId}")
    public ChatResponseDTO renameChat(@PathVariable Long chatId, @Valid @RequestBody NameUpdateRequestDTO request) {
        return chatService.renameChat(chatId, request);
    }

    @DeleteMapping("/{chatId}")
    public ResponseEntity<Void> deleteChat(@PathVariable Long chatId) {
        chatService.deleteChat(chatId);
        return ResponseEntity.noContent().build();
    }
}
