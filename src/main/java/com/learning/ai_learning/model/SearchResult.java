package com.learning.ai_learning.model;

public record SearchResult(
        String content,
        String truckModel,
        String category,
        String severity
) { }
