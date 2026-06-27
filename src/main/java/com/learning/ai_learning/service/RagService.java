package com.learning.ai_learning.service;

import com.learning.ai_learning.model.RagResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RagService {

    private static final Logger log = LoggerFactory.getLogger(RagService.class);
    public ChatClient chatClient;

    public VectorStore vectorStore;

    public RagService(VectorStore vectorStore, ChatClient.Builder chatClient) {
        this.vectorStore = vectorStore;
        this.chatClient = chatClient.defaultSystem("""
                You are an expert assistant for Cat MineStar Fleet Management System.
                You help mine operators and fleet supervisors troubleshoot equipment issues.
                Answer ONLY using the context provided below.
                If the answer is not in the context, say:
                'I don't have enough information in the equipment database to answer this question.'
                Always mention which truck model the issue applies to.
                Be concise and actionable.
                """)
                .build();
    }


    public RagResponse ask(String question) {
        log.info("RAG question received: {}", question);

        //step1: Retrieve the document from the pgvector
        List<Document> vectorDoc = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(question)
                        .topK(2)
                        .similarityThreshold(0.5)
                        .build()
        );

        log.info("Retrieved {} relevant documents from pgvector.", vectorDoc.size());

        if (vectorDoc.isEmpty()) {
            log.warn("No relevant documents found for question: {}", question);
            return new RagResponse(
                    "I don't have enough information in the equipment database to answer this question.",
                    List.of(),
                    question
            );
        }

        //step2: creating a context
        String context = vectorDoc.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n---\n"));

        log.info("Built context from {} documents", vectorDoc.size());

        //step3: Building a sources
        List<String> source = vectorDoc.stream()
                .map(document -> "Truck: " + document.getMetadata().get("truckModel") +
                        "| Category: " + document.getMetadata().get("category") +
                        "| Severity: " + document.getMetadata().get("severity"))
                .toList();

        log.info("Built sources from {} documents", vectorDoc.size());

        //step4: sending the data to the llm based on the document and getting the answer for a question
        String answer = chatClient.prompt()
                .user(u -> u.text("""
                        Context from Mining equipment database:
                        {context}
                        
                        Question: {question}
                        
                        Provide a clear, actionable answer based only on the context above.""")
                        .param("context", context)
                        .param("question", question))
                .call()
                .content();

        log.info("RAG answer generated successfully");

        return new RagResponse(answer, source, question);

    }

}
