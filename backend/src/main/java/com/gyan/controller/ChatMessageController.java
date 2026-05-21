package com.gyan.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gyan.dto.ChatMessageResponseDTO;
import com.gyan.service.ChatMessageService;

@RestController
@RequestMapping("/chats")
public class ChatMessageController {
    private final ChatMessageService chatMessageService;

    public ChatMessageController(ChatMessageService chatMessageService) {
        this.chatMessageService = chatMessageService;
    }

    @GetMapping("/{chatId}/messages")
    public List<ChatMessageResponseDTO> getMessages(@PathVariable Long chatId) {
        return chatMessageService.getMessages(chatId);
    }
}
