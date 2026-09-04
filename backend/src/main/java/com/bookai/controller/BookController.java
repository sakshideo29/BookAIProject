package com.bookai.controller;

import com.bookai.dto.BookRecommendation;
import com.bookai.dto.RecommendationRequest;
import com.bookai.service.BookRecommendationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookRecommendationService recommendationService;

    @PostMapping("/recommend")
    public ResponseEntity<List<BookRecommendation>> recommend(
            @Valid @RequestBody RecommendationRequest request) {
        List<BookRecommendation> results = recommendationService.recommend(request);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("BookAI backend is running");
    }
}
