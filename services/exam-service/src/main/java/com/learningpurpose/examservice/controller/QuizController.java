package com.learningpurpose.examservice.controller;

import com.learningpurpose.examservice.dto.ExamResultDto;
import com.learningpurpose.examservice.dto.ExamSubmissionDto;
import com.learningpurpose.examservice.dto.QuizDto;
import com.learningpurpose.examservice.model.Quiz;
import com.learningpurpose.examservice.service.QuizService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @PostMapping
    public ResponseEntity<Quiz> addQuiz(@Valid @RequestBody QuizDto quizDto) {
        return new ResponseEntity<>(quizService.addQuiz(quizDto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Quiz> updateQuiz(@PathVariable Long id, @Valid @RequestBody QuizDto quizDto) {
        return ResponseEntity.ok(quizService.updateQuiz(id, quizDto));
    }

    @GetMapping
    public ResponseEntity<List<Quiz>> getQuizzes() {
        return ResponseEntity.ok(quizService.getQuizzes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Quiz> getQuiz(@PathVariable Long id) {
        return ResponseEntity.ok(quizService.getQuiz(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuiz(@PathVariable Long id) {
        quizService.deleteQuiz(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<Quiz>> getQuizzesOfCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(quizService.getQuizzesOfCategory(categoryId));
    }

    @GetMapping("/active")
    public ResponseEntity<List<Quiz>> getActiveQuizzes() {
        return ResponseEntity.ok(quizService.getActiveQuizzes());
    }

    @GetMapping("/category/active/{categoryId}")
    public ResponseEntity<List<Quiz>> getActiveQuizzesOfCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(quizService.getActiveQuizzesOfCategory(categoryId));
    }

    @PostMapping("/eval-quiz")
    public ResponseEntity<ExamResultDto> evaluateQuiz(@Valid @RequestBody ExamSubmissionDto submission) {
        return ResponseEntity.ok(quizService.evaluateQuiz(submission));
    }
}