package com.learning.ai_learning.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import javax.xml.namespace.QName;

@Service
public class LlmResilenceService {
    private static final Logger log = LogManager.getLogger(LlmResilenceService.class);
    private final ChatClient chatClient;

    public LlmResilenceService (ChatClient.Builder chatBuilder) {
        this.chatClient = chatBuilder.
                defaultSystem("""
                        You are an expert assistant for Cat MineStar Fleet Management System.
                        Answer concisely and professionally.
                        """)
                .build();
    }

    // Retry 3 times with exponential backoff
    // Circuit breaker opens after 50% failure rate
    @CircuitBreaker(name = "llmservice", fallbackMethod = "fallbackResponse")
    @Retry(name = "llmservice")
    public String askWithResilence(String question) {
        log.info("Calling LLM with resilience for: {}", question);

        return chatClient.prompt()
                .user(question)
                .call()
                .content();
    }

    // Fallback — called when circuit is open or all retries fail
    public String fallbackResponse(String question, Exception ex) {
        log.warn("LLM fallback triggered for question: {}. Reason: {}", question, ex.getMessage());

        return """
                The AI assistant is temporarily unavailable.
                Please try again in a few minutes.
                For urgent fleet issues contact your site supervisor directly.
                Error: %s
                """.formatted(ex.getMessage());
    }
}
