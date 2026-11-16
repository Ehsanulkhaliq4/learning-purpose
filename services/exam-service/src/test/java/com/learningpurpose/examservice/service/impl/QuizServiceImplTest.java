package com.learningpurpose.examservice.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learningpurpose.examservice.model.Quiz;
import com.learningpurpose.examservice.repository.CategoryRepository;
import com.learningpurpose.examservice.repository.QuestionRepository;
import com.learningpurpose.examservice.repository.QuizRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuizServiceImplTest {

    @Mock private QuizRepository quizRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private QuestionRepository questionRepository;
    @Mock private KafkaTemplate<String, String> kafkaTemplate;
    @Mock private ObjectMapper objectMapper;
    @InjectMocks private QuizServiceImpl quizService;

    @Test
    void quizCanBeActivatedAndDeactivated() {
        Quiz quiz = Quiz.builder().id(10L).active(false).build();
        when(quizRepository.findById(10L)).thenReturn(Optional.of(quiz));
        when(quizRepository.save(quiz)).thenReturn(quiz);

        Quiz activated = quizService.updateQuizStatus(10L, true);
        assertTrue(activated.isActive());
        verify(quizRepository).save(quiz);

        Quiz deactivated = quizService.updateQuizStatus(10L, false);
        assertFalse(deactivated.isActive());
        verify(quizRepository, org.mockito.Mockito.times(2)).save(quiz);
    }
}
