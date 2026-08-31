package com.learningpurpose.examservice.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learningpurpose.examservice.dto.ExamResultDto;
import com.learningpurpose.examservice.dto.ExamSubmissionDto;
import com.learningpurpose.examservice.dto.QuizDto;
import com.learningpurpose.examservice.event.ExamSubmittedEvent;
import com.learningpurpose.examservice.exception.ResourceNotFoundException;
import com.learningpurpose.examservice.model.Category;
import com.learningpurpose.examservice.model.Question;
import com.learningpurpose.examservice.model.Quiz;
import com.learningpurpose.examservice.repository.CategoryRepository;
import com.learningpurpose.examservice.repository.QuestionRepository;
import com.learningpurpose.examservice.repository.QuizRepository;
import com.learningpurpose.examservice.service.QuizService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuizServiceImpl implements QuizService {

    private final QuizRepository quizRepository;
    private final CategoryRepository categoryRepository;
    private final QuestionRepository questionRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private static final String TOPIC_EXAM_SUBMITTED = "exam-submitted-topic";

    @Override
    public Quiz addQuiz(QuizDto dto) {
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + dto.getCategoryId()));

        Quiz quiz = Quiz.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .maxMarks(dto.getMaxMarks())
                .numberOfQuestions(dto.getNumberOfQuestions())
                .active(dto.isActive())
                .category(category)
                .build();

        return quizRepository.save(quiz);
    }

    @Override
    public Quiz updateQuiz(Long id, QuizDto dto) {
        Quiz quiz = getQuiz(id);
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + dto.getCategoryId()));

        quiz.setTitle(dto.getTitle());
        quiz.setDescription(dto.getDescription());
        quiz.setMaxMarks(dto.getMaxMarks());
        quiz.setNumberOfQuestions(dto.getNumberOfQuestions());
        quiz.setActive(dto.isActive());
        quiz.setCategory(category);

        return quizRepository.save(quiz);
    }

    @Override
    public List<Quiz> getQuizzes() {
        return quizRepository.findAll();
    }

    @Override
    public Quiz getQuiz(Long quizId) {
        return quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with id: " + quizId));
    }

    @Override
    public void deleteQuiz(Long quizId) {
        Quiz quiz = getQuiz(quizId);
        quizRepository.delete(quiz);
    }

    @Override
    public List<Quiz> getQuizzesOfCategory(Long categoryId) {
        return quizRepository.findByCategoryId(categoryId);
    }

    @Override
    public List<Quiz> getActiveQuizzes() {
        return quizRepository.findByActiveTrue();
    }

    @Override
    public List<Quiz> getActiveQuizzesOfCategory(Long categoryId) {
        return quizRepository.findByCategoryIdAndActiveTrue(categoryId);
    }

    @Override
    public ExamResultDto evaluateQuiz(ExamSubmissionDto submission) {
        Quiz quiz = getQuiz(submission.getQuizId());
        List<Question> questions = questionRepository.findByQuizId(quiz.getId());
        Map<Long, String> userAnswers = submission.getSelectedAnswers();
        int correctAnswers = 0;
        int attempted = 0;
        double marksScored = 0.0;
        double singleQuestionMarks = questions.isEmpty() ? 0.0 : (double) quiz.getMaxMarks() / questions.size();
        for (Question q : questions){
            String selectedOption = userAnswers.get(q.getId());
            if (selectedOption != null && !selectedOption.trim().isEmpty()){
                attempted++;
                if (q.getAnswer().trim().equalsIgnoreCase(selectedOption.trim())) {
                    correctAnswers++;
                    marksScored += singleQuestionMarks;
                }
            }
        }
        double percentage = (quiz.getMaxMarks() > 0) ? (marksScored / quiz.getMaxMarks()) * 100.0 : 0.0;
        boolean passed = percentage >= 50.0;

        ExamResultDto result = ExamResultDto.builder()
                .quizId(quiz.getId())
                .quizTitle(quiz.getTitle())
                .totalQuestions(questions.size())
                .attempted(attempted)
                .correctAnswers(correctAnswers)
                .marksScored(Math.round(marksScored * 100.0) / 100.0)
                .maxMarks(quiz.getMaxMarks())
                .percentage(Math.round(percentage * 100.0) / 100.0)
                .passed(passed)
                .build();

        if (submission.getUserEmail() != null && !submission.getUserEmail().isBlank()) {
            ExamSubmittedEvent event = ExamSubmittedEvent.builder()
                    .quizId(quiz.getId())
                    .quizTitle(quiz.getTitle())
                    .username(submission.getUsername())
                    .userEmail(submission.getUserEmail())
                    .marksScored(result.getMarksScored())
                    .maxMarks(result.getMaxMarks())
                    .percentage(result.getPercentage())
                    .passed(result.isPassed())
                    .submittedAt(Instant.now())
                    .build();
            try {
                String payload = objectMapper.writeValueAsString(event);
                kafkaTemplate.send(TOPIC_EXAM_SUBMITTED, submission.getUserEmail(), payload);
            } catch (JsonProcessingException e) {
                log.error("Error serializing ExamSubmittedEvent for user {}: {}", submission.getUserEmail(), e.getMessage());
            }
        }

        return result;
    }
}
