package com.learning.ai_learning;

public record TruckAnalysis(
        String truckId,
        String status,
        String issue,
        String recommendation,
        String severity
) {
}
