package com.bookai.service;

import com.bookai.dto.RecommendationRequest;
import org.springframework.stereotype.Component;

@Component
public class RecommendationPromptBuilder {

    public String build(RecommendationRequest request) {
        return build(request, null);
    }

    public String build(RecommendationRequest request, String retrievedContext) {
        StringBuilder criteria = new StringBuilder();

        if (notBlank(request.getGenre())) {
            criteria.append("Genre preference: ").append(request.getGenre()).append(". ");
        }
        if (notBlank(request.getAuthor())) {
            criteria.append("The reader likes the author: ").append(request.getAuthor())
                   .append(". Suggest books by them or similar authors/style. ");
        }
        if (notBlank(request.getMood())) {
            criteria.append("The reader's current mood is: \"").append(request.getMood())
                   .append("\". Pick books that genuinely fit this mood/emotional state. ");
        }
        if (criteria.isEmpty()) {
            criteria.append("The reader has no strong preference — suggest a well-rounded, ");
            criteria.append("widely acclaimed mix of books. ");
        }

        int count = request.getCount() != null ? request.getCount() : 5;
        String contextBlock = "";
        if (notBlank(retrievedContext)) {
            contextBlock = "\n\nUse the following retrieved book knowledge as context to improve recommendations:\n" + retrievedContext + "\n\n";
        }

        return """
                You are a knowledgeable book recommendation assistant.
 
                %s
                %s
 
                Suggest exactly %d REAL, PUBLISHED books that match these criteria.
                Only recommend books that actually exist — do not invent titles or authors.
                Use accurate, real page counts (paperback edition) to the best of your knowledge.
                Prioritize the retrieved context when it is relevant.
 
                Return ONLY a JSON array (no markdown, no commentary, no code fences) where each
                element has exactly this shape:
                {
                  "title": "string",
                  "author": "string",
                  "pageCount": integer,
                  "genre": "string",
                  "shortSummary": "2-3 sentence spoiler-free summary"
                }
                """.formatted(criteria.toString().trim(), contextBlock, count);
    }

    private boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }
}
