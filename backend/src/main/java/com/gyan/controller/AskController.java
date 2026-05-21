package com.gyan.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gyan.dto.AskRequestDTO;
import com.gyan.service.QuestionAnswerService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/ai")
public class AskController {
    private final QuestionAnswerService qaService;

    public AskController(QuestionAnswerService qaService) {
        this.qaService = qaService;
    }

    @PostMapping("/ask")
    public java.util.Map<String, String> ask(@Valid @RequestBody AskRequestDTO request) throws Exception {
        String answer = qaService.askQuestion(request.getQuestion());
        return java.util.Map.of("answer", answer);
    }

    @PostMapping("/chats/{chatId}/ask")
    public java.util.Map<String, String> askForChat(
        @PathVariable Long chatId,
        @Valid @RequestBody AskRequestDTO request
    ) throws Exception {
        String answer = qaService.askQuestion(chatId, request.getQuestion());
        return java.util.Map.of("answer", answer);
    }
}
