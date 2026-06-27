package com.learning.ai_learning;

import java.util.List;

public record RagResponse(
        String answer,
        List<String> sources,
        String question
) {
}
