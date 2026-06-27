package com.learning.ai_learning.model;


import java.util.List;

public record AssistantResponse(
        String answer,
        List<String> source,
        String question
) { }
