package com.learning.ai_learning.controller;

import com.learning.ai_learning.model.AssistantResponse;
import com.learning.ai_learning.model.RagResponse;
import com.learning.ai_learning.model.SearchResult;
import com.learning.ai_learning.model.TruckAnalysis;
import com.learning.ai_learning.service.FleetAssistantService;
import com.learning.ai_learning.service.MineStarAssistantService;
import com.learning.ai_learning.service.RagService;
import com.learning.ai_learning.service.VectorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
public class AiController {

    private static final Logger log = LoggerFactory.getLogger(AiController.class);
    private final ChatClient chatClient;
    private final VectorService vectorService;
    private final RagService ragService;
    private final FleetAssistantService fleetAssistantService;
    private final MineStarAssistantService mineStarAssistantService;

    public AiController(ChatClient.Builder chatClient, VectorService vectorService,
                        RagService ragService, FleetAssistantService fleetAssistantService,
                        MineStarAssistantService mineStarAssistantService) {
        this.vectorService =vectorService;
        this.ragService = ragService;
        this.fleetAssistantService = fleetAssistantService;
        this.mineStarAssistantService =mineStarAssistantService;
        this.chatClient = chatClient
                .defaultSystem("""
                       You are an expert assistant for Cat MineStar Fleet Management System.
                       You help mine operators, fleet supervisors, and site managers
                       understand equipment performance, fault codes, and KPI reports.
                       Answer concisely and professionally.
                       If a question is not related to mining operations or fleet management,
                       politely say it is outside your area of expertise. 
                       """)
                .build();
    }

    @GetMapping("/ask")
    public String ask(@RequestParam String questions) {
        log.info("Question Received: {}", questions);

        String res = chatClient.prompt()
                .user(questions)
                .call()
                .content();

        log.info("AI Response: {}", res);

        return res;
    }

    @GetMapping(value = "/ask/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> askStream(@RequestParam String question) {
        log.info("Streaming question: {}", question);

        return chatClient.prompt()
                .user(question)
                .stream().content()
                .doOnNext(System.out::println)
                .doOnComplete(() -> System.out.println("\n--- Stream Complete ---"));
    }


    @GetMapping("/analyse")
    public TruckAnalysis analysis(@RequestParam String truckId,
                                  @RequestParam String problem) {

        log.info("Analyze Request - Truck: {}, Problem:{}", truckId, problem);

        BeanOutputConverter<TruckAnalysis> converter = new BeanOutputConverter<>(TruckAnalysis.class);

        String res = chatClient.prompt()
                .user(u->u.text("""
                        Analyse the following mining truck issue and respond\s
                        in the exact JSON format specified below.
                        
                        Truck ID: {truckId}
                        Problem: {problem}
                        
                        {format}
                        """)
                        .param("truckId", truckId)
                        .param("problem", problem)
                        .param("format", converter.getFormat()))
                .call()
                .content();

        log.info("RAW AI response: {}", res);

        TruckAnalysis result = converter.convert(res);

        log.info("Passed result: {}", result);
        return result;
    }

    @GetMapping("/vector/store")
    public String getTheStore() {
        vectorService.storeEquipmentIssues();
        return "6 Equipment vector issues are stored in pgvector Successfully! ";
    }

    @GetMapping("/vector/search")
    public List<SearchResult> searchTheQuery(@RequestParam String query) {

        log.info("Vector search request: {}", query);

        return vectorService.searchSimilarIssues(query, 2);
    }

    // RAG endpoint — answers from your own MineStar equipment database
    @GetMapping("/rag/ask")
    public RagResponse askRagQuestion(@RequestParam String question) {
        log.info("RAG endpoint called: {}", question);
        return ragService.ask(question);
    }

    @GetMapping("/fleet/chat")
    public String fleetChat(@RequestParam String message) {
        log.info("Fleet chat request: {}", message);
        return fleetAssistantService.chat(message);
    }

    // Capstone endpoint — unified MineStar AI Assistant
    @GetMapping("/assistant/chat")
    public AssistantResponse assistantChat(@RequestParam String question) {
        log.info("Assistant chat: {}", question);
        return mineStarAssistantService.chat(question);
    }

}
