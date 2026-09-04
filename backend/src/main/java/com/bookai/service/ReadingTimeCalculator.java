package com.bookai.service;

import org.springframework.stereotype.Component;

@Component
public class ReadingTimeCalculator {

    private static final int WORDS_PER_PAGE = 275;   // average paperback
    private static final int READING_SPEED_WPM = 200; // average adult reading speed

    /**
     * Returns [hours, days] to finish the book, given the reader's
     * available reading minutes per day.
     */
    public Result estimate(Integer pageCount, Integer minutesPerDay) {
        if (pageCount == null || pageCount <= 0) {
            return new Result(0, 0.0);
        }
        int safeMinutesPerDay = (minutesPerDay == null || minutesPerDay <= 0) ? 30 : minutesPerDay;

        double totalWords = pageCount * (double) WORDS_PER_PAGE;
        double totalMinutes = totalWords / READING_SPEED_WPM;
        int totalHours = (int) Math.ceil(totalMinutes / 60.0);

        double days = totalMinutes / safeMinutesPerDay;
        double roundedDays = Math.round(days * 10.0) / 10.0; // 1 decimal place

        return new Result(totalHours, roundedDays);
    }

    public record Result(int hours, double days) {}
}
