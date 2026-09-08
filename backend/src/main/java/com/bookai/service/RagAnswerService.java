package com.bookai.service;
import com.bookai.dto.DocumentChunk;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagAnswerService {

    private final RetrieverService retrieverService;
    private final GeminiClient geminiClient;

    /**
     * The actual "retrieval-augmented generation" step:
     * 1. Retrieve the top-k most relevant chunks for the query (existing RetrieverService).
     * 2. Build a prompt that grounds Gemini's answer in ONLY that retrieved content.
     * 3. Ask Gemini to generate a natural-language answer.
     */
    public String answer(String query, int topK) {
        List<DocumentChunk> chunks = retrieverService.retrieve(query, topK);

        if (chunks.isEmpty()) {
            return "I couldn't find any relevant information in the ingested documents to answer that.";
        }

        String prompt = buildPrompt(query, chunks);
        return geminiClient.generateContent(prompt, false);
    }

    private String buildPrompt(String query, List<DocumentChunk> chunks) {
        StringBuilder context = new StringBuilder();
        for (int i = 0; i < chunks.size(); i++) {
            DocumentChunk chunk = chunks.get(i);
            context.append("[Source ").append(i + 1)
                    .append(" - ").append(chunk.getSource()).append("]\n")
                    .append(chunk.getContent())
                    .append("\n\n");
        }

        return """
                You are a helpful assistant answering questions using ONLY the context provided below.
                If the answer is not contained in the context, say you don't have enough information —
                do not make up an answer.

                Context:
                %s

                Question: %s

                Answer clearly and concisely, based only on the context above.
                """.formatted(context.toString(), query);
    }
}
