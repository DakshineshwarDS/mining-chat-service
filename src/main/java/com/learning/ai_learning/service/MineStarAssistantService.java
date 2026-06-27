package com.learning.ai_learning.service;

import com.learning.ai_learning.model.AssistantResponse;
import com.learning.ai_learning.tools.TruckDataService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MineStarAssistantService {

    private static final Logger log = LogManager.getLogger(MineStarAssistantService.class);
    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final TruckDataService truckDataService;

    public MineStarAssistantService(ChatClient.Builder chatClient, VectorStore vectorStore,
                                    TruckDataService truckDataService) {
        this.vectorStore =vectorStore;
        this.truckDataService = truckDataService;
        this.chatClient = chatClient.defaultSystem(
                """
                        You are an intelligent AI assistant for Cat MineStar Fleet Management System.
                        You help mine operators, fleet supervisors, and site managers.
                        
                        You have two capabilities:
                        1. TOOLS: Access real-time truck data, fuel levels, fleet summary,
                           and create maintenance tickets using your tools.
                        2. KNOWLEDGE BASE: Answer questions about equipment issues,
                           fault codes, and troubleshooting from the equipment database.
                        
                        Decision rules:
                        - If the question mentions a SPECIFIC truck ID like CAT-793-007 → ALWAYS use tools first
                        - If asked about fleet summary or fuel levels → ALWAYS use tools
                        - If asked about general equipment issues or troubleshooting → use knowledge base context
                        - If asked to create a maintenance ticket → ALWAYS use createMaintenanceTicket tool
                        - Always cite sources when answering from knowledge base
                        - Always be concise, professional, and actionable
                        - Never make up truck data — always use tools for real-time info
                        """)
                .defaultTools(truckDataService)
                .build();

    }

    public AssistantResponse chat(String question) {

        log.info("Assistant received: {}", question);
        List<Document> vectorDoc = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(question)
                        .topK(2)
                        .similarityThreshold(0.5)
                        .build()
        );
        log.info("Retrieved {} relevant docs from knowledge base", vectorDoc.size());

        String context = vectorDoc.isEmpty() ? "No relevant equipment data found! " :
                vectorDoc.stream()
                          .map(Document::getText)
                          .collect(Collectors.joining("\n---\n"));

        List<String> source = vectorDoc.stream()
                .map(d -> d.getMetadata().get("truckModel") + "|" +
                        d.getMetadata().get("category") + "|" +
                        d.getMetadata().get("severity"))
                .toList();

        String answer = chatClient.prompt()
                .user(u->u.text("""
                        Equipment Knowledge Base Context:
                        {context}
                        
                        User Question: {question}
                        
                        Use your tools for real-time truck data.
                        Use the knowledge base context for troubleshooting questions.""")
                        .param("context", context)
                        .param("question", question))
                .call()
                .content();

        log.info("Assistant response generated");
        return new AssistantResponse(answer, source, question);
    }



}
