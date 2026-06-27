package com.learning.ai_learning.service;

import com.learning.ai_learning.tools.TruckDataService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class FleetAssistantService {

    private static final Logger log = LogManager.getLogger(FleetAssistantService.class);
    private final ChatClient chatClient;
    private final TruckDataService truckDataService;

    public FleetAssistantService(ChatClient.Builder chatclient , TruckDataService truckDataService) {
        this.truckDataService =truckDataService;
        this.chatClient = chatclient.defaultSystem("""
                You are an intelligent fleet assistant for Cat MineStar Fleet Management System.
                You have access to real-time truck data and can take actions on behalf of operators.
                When asked about truck status, fuel, or fleet information — always use your tools
                to get real data. Never guess or make up truck information.
                When a truck needs maintenance, proactively create a ticket.
                Be concise and professional in your responses.
                """)
                .defaultTools(truckDataService)
                .build();
    }

    public String chat(String userMessage) {
        log.info("Fleet assistant received: {}", userMessage);

        String response = chatClient.prompt()
                .user(userMessage)
                .call()
                .content();

        log.info("FleetAssistant response: {}", response);
        return response;
    }
}
