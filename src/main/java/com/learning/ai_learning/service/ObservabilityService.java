package com.learning.ai_learning.service;

import com.learning.ai_learning.model.DashboardStats;
import com.learning.ai_learning.model.LlmCallMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ObservabilityService {

    private static final Logger log = LoggerFactory.getLogger(ObservabilityService.class);

    // Thread-safe counters
    private final AtomicInteger totalCalls       = new AtomicInteger(0);
    private final AtomicInteger totalInputTokens  = new AtomicInteger(0);
    private final AtomicInteger totalOutputTokens = new AtomicInteger(0);
    private final AtomicLong    totalLatencyMs    = new AtomicLong(0);
    private double totalCostUsd = 0.0;

    // Per-endpoint stats
    private final Map<String, AtomicInteger> callsPerEndpoint = new ConcurrentHashMap<>();

    // Recent call history (last 20)
    private final List<LlmCallMetrics> recentCalls = new ArrayList<>();

    public synchronized void record(LlmCallMetrics metrics) {
        totalCalls.incrementAndGet();
        totalInputTokens.addAndGet(metrics.inputTokens());
        totalOutputTokens.addAndGet(metrics.outputTokens());
        totalLatencyMs.addAndGet(metrics.latencyMs());
        totalCostUsd += metrics.costUsd();

        callsPerEndpoint
                .computeIfAbsent(metrics.endpoint(), k -> new AtomicInteger(0))
                .incrementAndGet();

        if (recentCalls.size() >= 20) recentCalls.remove(0);
        recentCalls.add(metrics);
    }

    public DashboardStats getDashboardStats() {
        int calls = totalCalls.get();
        double avgLatency = calls > 0
                ? (double) totalLatencyMs.get() / calls : 0;

        return new DashboardStats(
                calls,
                totalInputTokens.get(),
                totalOutputTokens.get(),
                avgLatency,
                totalCostUsd,
                callsPerEndpoint,
                new ArrayList<>(recentCalls)
        );
    }
}
