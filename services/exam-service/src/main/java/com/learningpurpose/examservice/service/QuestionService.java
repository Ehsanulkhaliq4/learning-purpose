package com.learningpurpose.examservice.service;

import com.learningpurpose.examservice.dto.QuestionDto;
import com.learningpurpose.examservice.model.Question;

import java.util.List;

public interface QuestionService {
    Question addQuestion(QuestionDto questionDto);
    Question updateQuestion(Long id, QuestionDto questionDto);
    List<Question> getQuestionsOfQuiz(Long quizId);
    Question getQuestion(Long questionId);
    void deleteQuestion(Long questionId);
}
