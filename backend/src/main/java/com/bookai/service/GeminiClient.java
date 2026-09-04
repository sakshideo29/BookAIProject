package com.bookai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Slf4j
@Component
public class GeminiClient {

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.model}")
    private String model;

    public GeminiClient(@Value("${gemini.api.base-url}") String baseUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    /**
     * Sends a prompt to Gemini and returns the raw text of the model's reply.
     * We instruct Gemini (in the prompt) to return ONLY JSON, so callers
     * are expected to parse the returned string as JSON themselves.
     */
    public String generateContent(String prompt) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "GEMINI_API_KEY is not set. Run: export GEMINI_API_KEY=your_key_here");
        }

        Map<String, Object> requestBody = Map.of(
                "contents", new Object[]{
                        Map.of("parts", new Object[]{
                                Map.of("text", prompt)
                        })
                },
                "generationConfig", Map.of(
                        "temperature", 0.7,
                        "responseMimeType", "application/json"
                )
        );

        String uri = "/" + model + ":generateContent?key=" + apiKey;

        try {
            String rawResponse = webClient.post()
                    .uri(uri)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return extractText(rawResponse);
        } catch (Exception e) {
            log.error("Gemini API call failed", e);
            throw new RuntimeException("Failed to get a response from Gemini: " + e.getMessage(), e);
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
}
