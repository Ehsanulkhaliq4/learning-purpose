package com.learningpurpose.examservice.service;

import com.learningpurpose.examservice.dto.ExamResultDto;
import com.learningpurpose.examservice.dto.ExamSubmissionDto;
import com.learningpurpose.examservice.dto.QuizDto;
import com.learningpurpose.examservice.model.Quiz;

import java.util.List;

public interface QuizService {
    Quiz addQuiz(QuizDto quizDto);
    Quiz updateQuiz(Long id, QuizDto quizDto);
    List<Quiz> getQuizzes();
    Quiz getQuiz(Long quizId);
    void deleteQuiz(Long quizId);
    List<Quiz> getQuizzesOfCategory(Long categoryId);
    List<Quiz> getActiveQuizzes();
    List<Quiz> getActiveQuizzesOfCategory(Long categoryId);
    ExamResultDto evaluateQuiz(ExamSubmissionDto submission);
}