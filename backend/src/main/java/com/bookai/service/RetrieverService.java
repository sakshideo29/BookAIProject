package com.bookai.service;

import com.bookai.dto.DocumentChunk;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RetrieverService {

    private final VectorStoreRepository vectorStoreRepository;
    private final EmbeddingService embeddingService;

    /**
     * Retrieve top-k chunks for a given query text. This method will
     * call the embedding service to obtain a query vector and then
     * query the PGVector-backed repository.
     */
    public List<DocumentChunk> retrieve(String query, int topK) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("query must not be empty");
        }

        double[] queryEmbedding = embeddingService.embed(query);
        if (queryEmbedding == null) {
            throw new IllegalStateException("EmbeddingService returned null embedding");
        }

        return vectorStoreRepository.queryNearest(queryEmbedding, topK);
    }
}
