package com.bookai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;

@Slf4j
@Component
public class GeminiClient {

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.model:gemini-2.0-flash}")
    private String model;

    @Value("${gemini.embedding.model:gemini-embedding-001}")
    private String embeddingModel;

    @Value("${gemini.embedding.dimensions:768}")
    private int embeddingDimensions;

    private static final int MAX_RETRIES = 6;
    private static final long INITIAL_BACKOFF_MS = 2000;

    public GeminiClient(@Value("${gemini.api.base-url:https://generativelanguage.googleapis.com}") String baseUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public String generateContent(String prompt) {
        return generateContent(prompt, true);
    }

    public String generateContent(String prompt, boolean expectJson) {
        requireApiKey();

        Map<String, Object> generationConfig = expectJson
                ? Map.of("temperature", 0.7, "responseMimeType", "application/json")
                : Map.of("temperature", 0.7);

        Map<String, Object> requestBody = Map.of(
                "contents", new Object[]{
                        Map.of("parts", new Object[]{
                                Map.of("text", prompt)
                        })
                },
                "generationConfig", generationConfig
        );

        String uri = "/v1beta/models/" + model + ":generateContent?key=" + apiKey;

        String rawResponse = executeWithRetry(() -> webClient.post()
                .uri(uri)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block());

        try {
            return extractText(rawResponse);
        } catch (Exception e) {
            log.error("Failed to parse Gemini response", e);
            throw new RuntimeException("Failed to parse response from Gemini: " + e.getMessage(), e);
        }
    }

    private String extractText(String rawJsonResponse) throws Exception {
        JsonNode root = objectMapper.readTree(rawJsonResponse);
        return root
                .path("candidates").get(0)
                .path("content")
                .path("parts").get(0)
                .path("text")
                .asText();
    }

    /**
     * Embed a piece of text using Gemini embeddings, truncated to
     * embeddingDimensions (default 768) via MRL output dimensionality.
     * Automatically retries with exponential backoff on 429 (rate limit)
     * responses, since bulk PDF ingestion can easily exceed per-minute quotas.
     */
    public double[] generateEmbeddings(String text) {
        requireApiKey();

        String uri = "/v1beta/models/{model}:embedContent";
        Map<String, Object> requestBody = Map.of(
                "model", "models/" + embeddingModel,
                "content", Map.of(
                        "parts", new Object[]{
                                Map.of("text", text)
                        }
                ),
                "outputDimensionality", embeddingDimensions
        );

        String rawResponse = executeWithRetry(() -> webClient.post()
                .uri(uri, embeddingModel)
                .header("x-goog-api-key", apiKey)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block());

        try {
            JsonNode root = objectMapper.readTree(rawResponse);
            JsonNode embeddingNode = root.path("embedding").path("values");

            if (!embeddingNode.isArray()) {
                throw new RuntimeException("Embedding not found in Gemini response: " + rawResponse);
            }

            double[] vec = new double[embeddingNode.size()];
            for (int i = 0; i < embeddingNode.size(); i++) {
                vec[i] = embeddingNode.get(i).asDouble();
            }
            return vec;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to parse Gemini embeddings response", e);
            throw new RuntimeException("Failed to parse embeddings from Gemini: " + e.getMessage(), e);
        }
    }

    private void requireApiKey() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "GEMINI_API_KEY is not set. Run: export GEMINI_API_KEY=your_key_here");
        }
    }

    /**
     * Executes a Gemini HTTP call, retrying with exponential backoff on
     * 429 (rate limit) and 5xx (transient server) errors. Fails fast on
     * everything else (bad request, auth errors, etc.) since retrying
     * those would just waste time.
     */
    private String executeWithRetry(java.util.function.Supplier<String> call) {
        long backoffMs = INITIAL_BACKOFF_MS;
        RuntimeException lastError = null;

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                return call.get();
            } catch (WebClientResponseException e) {
                boolean retryable = e.getStatusCode().value() == 429 || e.getStatusCode().is5xxServerError();
                lastError = e;

                if (!retryable || attempt == MAX_RETRIES) {
                    log.error("Gemini API call failed (attempt {}/{}, status {})",
                            attempt, MAX_RETRIES, e.getStatusCode(), e);
                    throw new RuntimeException("Failed to get a response from Gemini: " + e.getMessage(), e);
                }

                log.warn("Gemini API call hit {} (attempt {}/{}). Retrying in {}ms...",
                        e.getStatusCode(), attempt, MAX_RETRIES, backoffMs);

                sleep(backoffMs);
                backoffMs = Math.min(backoffMs * 2, 30_000);
            } catch (Exception e) {
                log.error("Gemini API call failed with non-retryable error", e);
                throw new RuntimeException("Failed to get a response from Gemini: " + e.getMessage(), e);
            }
        }

        throw new RuntimeException("Failed to get a response from Gemini after retries", lastError);
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting to retry Gemini call", ie);
        }
    }
}