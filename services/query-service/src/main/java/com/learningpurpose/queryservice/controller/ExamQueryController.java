package com.learningpurpose.queryservice.controller;

import com.learningpurpose.queryservice.document.ExamCategoryDocument;
import com.learningpurpose.queryservice.document.QuizDocument;
import com.learningpurpose.queryservice.service.ExamQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public/exams")
@RequiredArgsConstructor
public class ExamQueryController {

    private final ExamQueryService examQueryService;


    // =========================================================
    // CATEGORIES
    // =========================================================

    @GetMapping("/categories")
    public List<ExamCategoryDocument> getCategories() {
        return examQueryService.getCategories();
    }


    @GetMapping("/categories/{id}")
    public ExamCategoryDocument getCategory(
            @PathVariable Long id) {

        return examQueryService.getCategory(id);
    }


    // =========================================================
    // QUIZZES
    // =========================================================

    @GetMapping("/quizzes")
    public List<QuizDocument> getQuizzes() {

        return examQueryService.getQuizzes();
    }


    @GetMapping("/quizzes/{id}")
    public QuizDocument getQuiz(
            @PathVariable Long id) {

        return examQueryService.getQuiz(id);
    }


    @GetMapping("/categories/{categoryId}/quizzes")
    public List<QuizDocument> getQuizzesByCategory(
            @PathVariable Long categoryId) {

        return examQueryService
                .getQuizzesByCategory(categoryId);
    }


    @GetMapping("/quizzes/active")
    public List<QuizDocument> getActiveQuizzes() {
        return examQueryService.getActiveQuizzes();
    }
}