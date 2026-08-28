package com.learningpurpose.examservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class QuizDto {
    private Long id;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Max marks is required")
    private Integer maxMarks;

    @NotNull(message = "Number of questions is required")
    private Integer numberOfQuestions;

    private boolean active;

    @NotNull(message = "Category ID is required")
    private Long categoryId;
}