package com.learningpurpose.aichatservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExamQuizSummary {
    private Long id;
    private String title;
    private String description;
    private Integer maxMarks;
    private Integer numberOfQuestions;
    private String categoryTitle;
}