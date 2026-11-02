package com.learningpurpose.userservice.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learningpurpose.userservice.model.User;
import com.learningpurpose.userservice.model.UserActivityLog;
import com.learningpurpose.userservice.model.UserDailyStudyRhythm;
import com.learningpurpose.userservice.model.UserLearningMetrics;
import com.learningpurpose.userservice.repository.UserActivityLogRepository;
import com.learningpurpose.userservice.repository.UserDailyStudyRhythmRepository;
import com.learningpurpose.userservice.repository.UserLearningMetricsRepository;
import com.learningpurpose.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ExamActivityConsumerTest {

    private ObjectMapper objectMapper;
    private UserRepository userRepository;
    private UserActivityLogRepository activityRepository;
    private UserLearningMetricsRepository metricsRepository;
    private UserDailyStudyRhythmRepository rhythmRepository;
    private ExamActivityConsumer consumer;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        userRepository = mock(UserRepository.class);
        activityRepository = mock(UserActivityLogRepository.class);
        metricsRepository = mock(UserLearningMetricsRepository.class);
        rhythmRepository = mock(UserDailyStudyRhythmRepository.class);

        consumer = new ExamActivityConsumer(
                objectMapper, userRepository, activityRepository, metricsRepository, rhythmRepository);
    }

    @Test
    void persistsExamActivityAndUpdatesAssessmentMetricsAndRhythm() {
        User user = User.builder().id(42L).email("learner@example.com").build();
        when(userRepository.findByEmail("learner@example.com")).thenReturn(Optional.of(user));
        when(metricsRepository.findByUserId(42L)).thenReturn(Optional.empty());
        when(rhythmRepository.findByUserIdAndStudyDate(eq(42L), any(LocalDate.class))).thenReturn(Optional.empty());

        consumer.onExamSubmitted("""
                {"userEmail":"learner@example.com","quizTitle":"Algorithms","percentage":87.5,"passed":true,"submittedAt":"2026-11-02T10:15:30Z"}
                """);

        ArgumentCaptor<UserActivityLog> activityCaptor = ArgumentCaptor.forClass(UserActivityLog.class);
        verify(activityRepository).save(activityCaptor.capture());
        assertEquals(user, activityCaptor.getValue().getUser());
        assertEquals("Completed Algorithms", activityCaptor.getValue().getTitle());
        assertEquals("Scored 87.5% - Passed", activityCaptor.getValue().getSubtitle());
        assertEquals("green", activityCaptor.getValue().getStatusColor());

        ArgumentCaptor<UserDailyStudyRhythm> rhythmCaptor = ArgumentCaptor.forClass(UserDailyStudyRhythm.class);
        verify(rhythmRepository).save(rhythmCaptor.capture());
        assertEquals(new BigDecimal("0.50"), rhythmCaptor.getValue().getHoursSpent());

        ArgumentCaptor<UserLearningMetrics> metricsCaptor = ArgumentCaptor.forClass(UserLearningMetrics.class);
        verify(metricsRepository).save(metricsCaptor.capture());
        assertEquals("87.50", metricsCaptor.getValue().getAssessmentScore().toPlainString());
        assertTrue(metricsCaptor.getValue().isPersonalBest());
        assertEquals(1, metricsCaptor.getValue().getCompletedLessons());
        assertEquals(1, metricsCaptor.getValue().getLessonsCompletedThisWeek());
        assertEquals(new BigDecimal("0.50"), metricsCaptor.getValue().getStudyTimeHours());
    }

    @Test
    void fallbacksToUsernameWhenEmailNotFound() {
        User user = User.builder().id(99L).username("testuser").email("other@example.com").build();
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(metricsRepository.findByUserId(99L)).thenReturn(Optional.empty());
        when(rhythmRepository.findByUserIdAndStudyDate(eq(99L), any(LocalDate.class))).thenReturn(Optional.empty());

        consumer.onExamSubmitted("""
                {"userEmail":"unknown@example.com","username":"testuser","quizTitle":"Java Basics","percentage":45.0,"passed":false}
                """);

        ArgumentCaptor<UserActivityLog> activityCaptor = ArgumentCaptor.forClass(UserActivityLog.class);
        verify(activityRepository).save(activityCaptor.capture());
        assertEquals("Completed Java Basics", activityCaptor.getValue().getTitle());
        assertEquals("Scored 45% - Not passed", activityCaptor.getValue().getSubtitle());
        assertEquals("red", activityCaptor.getValue().getStatusColor());
    }

    @Test
    void clampsPercentagesOutsideRange() {
        User user = User.builder().id(1L).email("user@example.com").build();
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(metricsRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(rhythmRepository.findByUserIdAndStudyDate(eq(1L), any(LocalDate.class))).thenReturn(Optional.empty());

        consumer.onExamSubmitted("""
                {"userEmail":"user@example.com","quizTitle":"","percentage":150.0,"passed":true}
                """);

        ArgumentCaptor<UserLearningMetrics> metricsCaptor = ArgumentCaptor.forClass(UserLearningMetrics.class);
        verify(metricsRepository).save(metricsCaptor.capture());
        assertEquals("100.00", metricsCaptor.getValue().getAssessmentScore().toPlainString());

        ArgumentCaptor<UserActivityLog> activityCaptor = ArgumentCaptor.forClass(UserActivityLog.class);
        verify(activityRepository).save(activityCaptor.capture());
        assertEquals("Completed Exam", activityCaptor.getValue().getTitle());
    }

    @Test
    void ignoresUnknownUserGracefully() {
        when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("")).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> consumer.onExamSubmitted("""
                {"userEmail":"ghost@example.com","quizTitle":"Test"}
                """));

        verifyNoInteractions(activityRepository);
        verifyNoInteractions(metricsRepository);
        verifyNoInteractions(rhythmRepository);
    }

    @Test
    void handlesMalformedJsonPayloadWithoutCrashing() {
        assertDoesNotThrow(() -> consumer.onExamSubmitted("{invalid-json"));
        assertDoesNotThrow(() -> consumer.onExamSubmitted(null));
        assertDoesNotThrow(() -> consumer.onExamSubmitted(""));

        verifyNoInteractions(activityRepository);
    }
}