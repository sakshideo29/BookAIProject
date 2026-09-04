package com.bookai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookRecommendation {
    private String title;
    private String author;
    private Integer pageCount;
    private String shortSummary;
    private String genre;

    /** Computed server-side, not by the LLM — see ReadingTimeCalculator */
    private Double estimatedReadingDays;
    private Integer estimatedReadingHours;
}
