package com.learningpurpose.examservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class ExamSubmissionDto {
    @NotNull(message = "Quiz ID is required")
    private Long quizId;

    @NotBlank(message = "Username is required")
    private String username;

    private String userEmail;

    // Mapping of questionId -> selectedOption
    @NotNull(message = "Selected answers map cannot be null")
    private Map<Long, String> selectedAnswers;
}