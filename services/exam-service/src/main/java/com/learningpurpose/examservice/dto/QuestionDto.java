package com.learningpurpose.examservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class QuestionDto {
    private Long id;

    @NotBlank(message = "Content is required")
    private String content;

    private String imageUrl;

    @NotEmpty(message = "Options list cannot be empty")
    private List<String> options;

    @NotBlank(message = "Answer is required")
    private String answer;

    private Integer marks = 1;

    @NotNull(message = "Quiz ID is required")
    private Long quizId;
}