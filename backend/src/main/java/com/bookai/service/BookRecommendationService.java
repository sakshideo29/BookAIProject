package com.bookai.service;

import com.bookai.dto.BookRecommendation;
import com.bookai.dto.RawBookSuggestion;
import com.bookai.dto.RecommendationRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookRecommendationService {

    private final GeminiClient geminiClient;
    private final RecommendationPromptBuilder promptBuilder;
    private final ReadingTimeCalculator readingTimeCalculator;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<BookRecommendation> recommend(RecommendationRequest request) {
        String prompt = promptBuilder.build(request);
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
