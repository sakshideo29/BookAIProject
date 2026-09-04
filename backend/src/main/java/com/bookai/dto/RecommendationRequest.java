package com.bookai.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationRequest {

    /** e.g. "Fantasy", "Thriller", "Non-fiction" — optional */
    private String genre;

    /** e.g. "Haruki Murakami" — optional; find similar authors/style */
    private String author;

    /** e.g. "stressed", "adventurous", "cozy rainy day" — optional */
    private String mood;

    @Min(1)
    @Max(10)
    private Integer count = 5;

    /** Minutes per day the user typically reads — used for reading-time prediction */
    @Min(5)
    @Max(600)
    private Integer minutesPerDay = 30;
}
