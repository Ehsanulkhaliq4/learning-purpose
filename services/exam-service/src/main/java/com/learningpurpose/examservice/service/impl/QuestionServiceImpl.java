package com.learningpurpose.examservice.service.impl;

import com.learningpurpose.examservice.dto.QuestionDto;
import com.learningpurpose.examservice.exception.ResourceNotFoundException;
import com.learningpurpose.examservice.model.Question;
import com.learningpurpose.examservice.model.Quiz;
import com.learningpurpose.examservice.repository.QuestionRepository;
import com.learningpurpose.examservice.repository.QuizRepository;
import com.learningpurpose.examservice.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private final QuestionRepository questionRepository;
    private final QuizRepository quizRepository;

    @Override
    public Question addQuestion(QuestionDto dto) {
        Quiz quiz = quizRepository.findById(dto.getQuizId())
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with id: " + dto.getQuizId()));

        Question question = Question.builder()
                .content(dto.getContent())
                .imageUrl(dto.getImageUrl())
                .options(dto.getOptions())
                .answer(dto.getAnswer())
                .marks(dto.getMarks() != null ? dto.getMarks() : 1)
                .quiz(quiz)
                .build();

        return questionRepository.save(question);
    }

    @Override
    public Question updateQuestion(Long id, QuestionDto dto) {
        Question question = getQuestion(id);
        Quiz quiz = quizRepository.findById(dto.getQuizId())
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with id: " + dto.getQuizId()));

        question.setContent(dto.getContent());
        question.setImageUrl(dto.getImageUrl());
        question.setOptions(dto.getOptions());
        question.setAnswer(dto.getAnswer());
        question.setMarks(dto.getMarks() != null ? dto.getMarks() : 1);
        question.setQuiz(quiz);

        return questionRepository.save(question);
    }

    @Override
    public List<Question> getQuestionsOfQuiz(Long quizId) {
        return questionRepository.findByQuizId(quizId);
    }

    @Override
    public Question getQuestion(Long questionId) {
        return questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id: " + questionId));
    }

    @Override
    public void deleteQuestion(Long questionId) {
        Question question = getQuestion(questionId);
        questionRepository.delete(question);
    }
}