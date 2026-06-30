package com.learning.ai_learning.model;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public record DashboardStats(
        int totalCalls,
        int totalInputTokens,
        int totalOutputTokens,
        double avgLatencyMs,
        double totalCostUsd,
        Map<String, AtomicInteger> callsPerEndpoint,
        List<LlmCallMetrics> recentCalls
) {
}
