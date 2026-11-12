package com.learningpurpose.userservice.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record UserProfileDto(
        Long id,
        String username,
        String firstName,
        String lastName,
        String email,
        String phone,
        String profile,
        boolean enabled,
        Instant createdAt,
        long quizAttempts,
        int completedLessons,
        BigDecimal bestScore,
        BigDecimal studyTimeHours,
        int currentStreakDays,
        List<QuizActivityDto> recentQuizAttempts
) {
    public record QuizActivityDto(String title, String subtitle, Instant completedAt) {}
}
