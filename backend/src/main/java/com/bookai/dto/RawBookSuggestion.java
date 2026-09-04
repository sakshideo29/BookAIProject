package com.bookai.dto;

import lombok.Data;

/**
 * Shape we ask Gemini to return per book, BEFORE we compute reading time
 * ourselves (never trust the LLM's own time estimate).
 */
@Data
public class RawBookSuggestion {
    private String title;
    private String author;
    private Integer pageCount;
    private String shortSummary;
    private String genre;
}
