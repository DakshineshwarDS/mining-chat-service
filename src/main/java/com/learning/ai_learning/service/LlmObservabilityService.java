package com.learning.ai_learning.service;

import com.learning.ai_learning.model.LlmCallMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LlmObservabilityService {

    private static final Logger log = LoggerFactory.getLogger(LlmObservabilityService.class);

    // GPT-4o-mini pricing (per 1M tokens)
    private static final double INPUT_COST_PER_TOKEN  = 0.15 / 1_000_000;
    private static final double OUTPUT_COST_PER_TOKEN = 0.60 / 1_000_000;

    public LlmCallMetrics recordCall(String endpoint,
                                     String question,
                                     String response,
                                     long startTimeMs) {
        long latencyMs      = System.currentTimeMillis() - startTimeMs;
        int  inputTokens    = estimateTokens(question);
        int  outputTokens   = estimateTokens(response);
        double costUsd      = (inputTokens  * INPUT_COST_PER_TOKEN)
                + (outputTokens * OUTPUT_COST_PER_TOKEN);

        LlmCallMetrics metrics = new LlmCallMetrics(
                endpoint,
                inputTokens,
                outputTokens,
                latencyMs,
                costUsd,
                "gpt-4o-mini"
        );

        log.info("LLM METRICS | endpoint={} | inputTokens={} | outputTokens={} " +
                        "| latencyMs={} | costUSD={} | model={}",
                metrics.endpoint(),
                metrics.inputTokens(),
                metrics.outputTokens(),
                metrics.latencyMs(),
                String.format("$%.6f", metrics.costUsd()),
                metrics.model()
        );

        return metrics;
    }

    // Simple token estimator — roughly 4 chars per token
    private int estimateTokens(String text) {
        if (text == null || text.isEmpty()) return 0;
        return (int) Math.ceil(text.length() / 4.0);
    }
}