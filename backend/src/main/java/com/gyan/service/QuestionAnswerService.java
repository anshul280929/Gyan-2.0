package com.gyan.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gyan.ai.LLMService;
import com.gyan.entity.Chat;
import com.gyan.entity.DocumentChunk;

@Service
public class QuestionAnswerService {
    private final SemanticSearchService semanticSearchService;
    private final LLMService llmService;
    private final ChatService chatService;
    private final ChatMessageService chatMessageService;
    private final AuditLogService auditLogService;
    private static final Logger log = LoggerFactory.getLogger(QuestionAnswerService.class);

    public QuestionAnswerService(
        SemanticSearchService semanticSearchService,
        LLMService llmService,
        ChatService chatService,
        ChatMessageService chatMessageService,
        AuditLogService auditLogService
    ) {
        this.semanticSearchService = semanticSearchService;
        this.llmService = llmService;
        this.chatService = chatService;
        this.chatMessageService = chatMessageService;
        this.auditLogService = auditLogService;
    }

    public String askQuestion(String question) throws Exception {
        log.info("Generating answers for : " + question);
        List<DocumentChunk> chunks = semanticSearchService.findRelevantChunks((question));
        return generateAnswer(question, chunks);
    }

    @Transactional
    public String askQuestion(Long chatId, String question) throws Exception {
        log.info("Generating answers for chat {} and question {}", chatId, question);
        List<DocumentChunk> chunks = semanticSearchService.findRelevantChunks(chatId, question);
        String answer = generateAnswer(question, chunks);
        Chat chat = chatService.getOwnedChat(chatId);
        chatMessageService.saveExchange(chat, question, answer);
        chatService.touch(chat);
        auditLogService.log("ai.ask", chat.getUser().getEmail(), "SUCCESS", "chatId=" + chatId + " questionLength=" + question.length());
        return answer;
    }

    private String generateAnswer(String question, List<DocumentChunk> chunks) throws Exception {
        StringBuilder context = new StringBuilder();

        for (DocumentChunk chunk : chunks) {
            context.append(chunk.getChunkText()).append("\n\n");
        }

        return llmService.generateAnswer(question, context.toString());
    }
}
