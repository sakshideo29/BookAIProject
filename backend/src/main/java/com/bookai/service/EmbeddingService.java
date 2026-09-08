package com.bookai.service;

import com.bookai.dto.DocumentChunk;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingService {

    private final GeminiClient geminiClient;

    /**
     * Produce embedding for a given text.
     * Currently delegates to GeminiClient.generateEmbeddings().
     * The GeminiClient method is a stub and must be implemented to call
     * the real embeddings endpoint.
     */
    public double[] embed(String text) {
        // normalization or trimming can happen here
        return geminiClient.generateEmbeddings(text);
    }

    /**
     * Helper to embed a DocumentChunk and attach the vector to the chunk.
     */
    public void embedChunk(DocumentChunk chunk) {
        double[] vec = embed(chunk.getContent());
        chunk.setEmbedding(vec);
    }
}
