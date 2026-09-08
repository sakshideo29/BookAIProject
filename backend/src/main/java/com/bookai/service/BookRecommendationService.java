package com.bookai.service;

import com.bookai.dto.BookRecommendation;
import com.bookai.dto.DocumentChunk;
import com.bookai.dto.RawBookSuggestion;
import com.bookai.dto.RecommendationRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookRecommendationService {

    private final GeminiClient geminiClient;
    private final RecommendationPromptBuilder promptBuilder;
    private final ReadingTimeCalculator readingTimeCalculator;
    private final RetrieverService retrieverService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<BookRecommendation> recommend(RecommendationRequest request) {
        String prompt = promptBuilder.build(request);
        String retrievedContext = null;

        try {
            String query = buildRetrievalQuery(request);
            List<DocumentChunk> matches = retrieverService.retrieve(query, 5);
            if (matches != null && !matches.isEmpty()) {
                retrievedContext = matches.stream()
                        .map(DocumentChunk::getContent)
                        .filter(content -> content != null && !content.isBlank())
                        .collect(Collectors.joining("\n\n"));
                prompt = promptBuilder.build(request, retrievedContext);
            }
        } catch (Exception e) {
            log.warn("RAG retrieval failed; continuing without retrieved context", e);
        }

        log.info("Requesting {} recommendations (genre={}, author={}, mood={})",
                request.getCount(), request.getGenre(), request.getAuthor(), request.getMood());

        String rawJson = geminiClient.generateContent(prompt);

        RawBookSuggestion[] rawSuggestions;
        try {
            rawSuggestions = objectMapper.readValue(rawJson, RawBookSuggestion[].class);
        } catch (Exception e) {
            log.error("Failed to parse Gemini response as JSON: {}", rawJson, e);
            throw new RuntimeException("Could not parse AI response. Try again.", e);
        }

        return List.of(rawSuggestions).stream()
                .map(raw -> toRecommendation(raw, request.getMinutesPerDay()))
                .toList();
    }

    private String buildRetrievalQuery(RecommendationRequest request) {
        StringBuilder query = new StringBuilder();
        if (request.getGenre() != null && !request.getGenre().isBlank()) {
            query.append(request.getGenre()).append(" ");
        }
        if (request.getAuthor() != null && !request.getAuthor().isBlank()) {
            query.append(request.getAuthor()).append(" ");
        }
        if (request.getMood() != null && !request.getMood().isBlank()) {
            query.append(request.getMood()).append(" ");
        }
        if (query.isEmpty()) {
            query.append("popular fiction");
        }
        return query.toString().trim();
    }

    private BookRecommendation toRecommendation(RawBookSuggestion raw, Integer minutesPerDay) {
        ReadingTimeCalculator.Result timing =
                readingTimeCalculator.estimate(raw.getPageCount(), minutesPerDay);

        BookRecommendation rec = new BookRecommendation();
        rec.setTitle(raw.getTitle());
        rec.setAuthor(raw.getAuthor());
        rec.setPageCount(raw.getPageCount());
        rec.setShortSummary(raw.getShortSummary());
        rec.setGenre(raw.getGenre());
        rec.setEstimatedReadingHours(timing.hours());
        rec.setEstimatedReadingDays(timing.days());
        return rec;
    }
}
