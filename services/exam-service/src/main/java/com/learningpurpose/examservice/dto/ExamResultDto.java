package com.learningpurpose.examservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExamResultDto {
    private Long quizId;
    private String quizTitle;
    private int totalQuestions;
    private int attempted;
    private int correctAnswers;
    private double marksScored;
    private double maxMarks;
    private double percentage;
    private boolean passed;
}