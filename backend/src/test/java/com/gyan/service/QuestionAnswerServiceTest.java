package com.gyan.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.gyan.ai.LLMService;
import com.gyan.entity.Chat;
import com.gyan.entity.DocumentChunk;
import com.gyan.entity.User;
import com.gyan.model.Role;

@ExtendWith(MockitoExtension.class)
class QuestionAnswerServiceTest {

    @Mock
    private SemanticSearchService semanticSearchService;

    @Mock
    private LLMService llmService;

    @Mock
    private ChatService chatService;

    @Mock
    private ChatMessageService chatMessageService;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private QuestionAnswerService qaService;

    @Test
    void testAskQuestion() throws Exception {

        DocumentChunk chunk = new DocumentChunk();
        chunk.setChunkText("Kafka is used for event-driven systems");

        when(semanticSearchService.findRelevantChunks(any()))
                .thenReturn(List.of(chunk));

        when(llmService.generateAnswer(any(), any()))
                .thenReturn("Kafka is used for event-driven systems");

        String answer = qaService.askQuestion("What is Kafka?");

        assertNotNull(answer);
        assertTrue(answer.contains("Kafka"));
    }

    @Test
    void testAskQuestionForChatPersistsHistory() throws Exception {
        DocumentChunk chunk = new DocumentChunk();
        chunk.setChunkText("Invoice total is 1250");

        Chat chat = new Chat();
        chat.setId(3L);
        chat.setUser(new User("owner@example.com", "hashed", Role.USER));

        when(semanticSearchService.findRelevantChunks(3L, "What is the invoice total?"))
            .thenReturn(List.of(chunk));
        when(llmService.generateAnswer(any(), any()))
            .thenReturn("The invoice total is 1250.");
        when(chatService.getOwnedChat(3L)).thenReturn(chat);

        String answer = qaService.askQuestion(3L, "What is the invoice total?");

        assertTrue(answer.contains("1250"));
        verify(chatMessageService).saveExchange(chat, "What is the invoice total?", "The invoice total is 1250.");
        verify(chatService).touch(chat);
    }
}
