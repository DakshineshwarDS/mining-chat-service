package com.learning.ai_learning;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class VectorService {

    private static final Logger log = LoggerFactory.getLogger(VectorService.class);
    public VectorStore vectorStore;

    public VectorService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    //store equipment issues into pgvector
    public void storeEquipmentIssues() {

        List<Document> documents = List.of(
                new Document("CAT 793 truck experiencing engine overheating during uphill haul cycles. " +
                                "Coolant temperature exceeds 105°C. Check radiator fins for dust blockage " +
                                "and verify coolant level.",
                        Map.of("truckModel", "CAT-793", "category", "engine", "severity", "HIGH")
                ),

                new Document("Payload variance detected on CAT 785 haul truck. " +
                                "Actual payload consistently 15% above target. " +
                                "Inspect shovel operator technique and recalibrate payload monitoring system.",
                        Map.of("truckModel", "CAT-785", "category", "payload", "severity", "MEDIUM")
                ),

                new Document("CAT 793 motor grader showing abnormal fuel consumption. " +
                                "Fuel usage 20% above baseline. Check fuel injectors for wear " +
                                "and inspect air filter for blockage.",
                        Map.of("truckModel", "CAT-793", "category", "fuel", "severity", "MEDIUM")
                ),

                new Document("Brake system pressure drop detected on CAT 789 truck. " +
                                "Brake accumulator pressure falling below 2000 PSI. " +
                                "Inspect brake lines for leaks and check accumulator charge.",
                        Map.of("truckModel", "CAT-789", "category", "brakes", "severity", "CRITICAL")
                ),

                new Document("CAT 793 haul truck turbocharger failure causing power loss. " +
                                "Engine output reduced by 30%. Replace turbocharger assembly " +
                                "and check intercooler for oil contamination.",
                        Map.of("truckModel", "CAT-793", "category", "engine", "severity", "HIGH")
                ),

                new Document("Transmission oil temperature warning on CAT 785. " +
                                "Oil temp exceeding 120°C during loaded haul cycles. " +
                                "Check transmission oil cooler and verify oil level.",
                        Map.of("truckModel", "CAT-785", "category", "transmission", "severity", "HIGH")
                )
        );

        vectorStore.add(documents);
        log.info("Stored {} equipment issues into pgvector", documents.size());
    }

    //Search for similar issues by meaning
    public List<SearchResult> searchSimilarIssues(String query, int topK) {
        log.info("Searching pgvector for: {}", query);

        List<Document> results = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(query)
                        .topK(topK)
                        .similarityThreshold(0.5).build()
        );

        log.info("Found {} similar issues", results.size());
        return results.stream()
                .map(document -> new SearchResult(
                        document.getText(),
                        (String) document.getMetadata().get("truckModel"),
                        (String) document.getMetadata().get("category"),
                        (String) document.getMetadata().get("severity")
                ))
                .toList();
    }
}
