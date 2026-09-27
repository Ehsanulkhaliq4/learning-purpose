package com.learningpurpose.queryservice.service;

import com.learningpurpose.queryservice.document.ExamCategoryDocument;
import com.learningpurpose.queryservice.document.QuizDocument;
import com.learningpurpose.queryservice.repository.ExamCategoryRepository;
import com.learningpurpose.queryservice.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExamQueryService {

    private final ExamCategoryRepository categoryRepository;
    private final QuizRepository quizRepository;


    public List<ExamCategoryDocument> getCategories() {

        return categoryRepository.findAll();
    }


    public ExamCategoryDocument getCategory(Long id) {

        return categoryRepository
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Category not found: " + id
                        )
                );
    }


    public List<QuizDocument> getQuizzes() {

        return quizRepository.findAll();
    }


    public QuizDocument getQuiz(Long id) {

        return quizRepository
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Quiz not found: " + id
                        )
                );
    }


    public List<QuizDocument> getQuizzesByCategory(
            Long categoryId) {

        return quizRepository
                .findByCategoryId(categoryId);
    }


    public List<QuizDocument> getActiveQuizzes() {

        return quizRepository.findByActiveTrue();
    }
}