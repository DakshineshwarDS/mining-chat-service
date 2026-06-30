package com.learning.ai_learning.model;

public record LlmCallMetrics(
        String endpoint,
        int inputTokens,
        int outputTokens,
        long latencyMs,
        double costUsd,
        String model
) {
}
