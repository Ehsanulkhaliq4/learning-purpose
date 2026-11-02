package com.learningpurpose.userservice.service;

import com.learningpurpose.userservice.dto.CreateActivityRequest;
import com.learningpurpose.userservice.dto.DashboardResponseDto;
import com.learningpurpose.userservice.dto.DashboardResponseDto.ActivityDto;
import com.learningpurpose.userservice.dto.DashboardResponseDto.DailyBarDto;
import com.learningpurpose.userservice.dto.DashboardResponseDto.ResumeItemDto;
import com.learningpurpose.userservice.dto.LogStudyTimeRequest;
import com.learningpurpose.userservice.dto.UpdateResumeItemRequest;
import com.learningpurpose.userservice.exception.ResourceNotFoundException;
import com.learningpurpose.userservice.model.User;
import com.learningpurpose.userservice.model.UserActivityLog;
import com.learningpurpose.userservice.model.UserDailyStudyRhythm;
import com.learningpurpose.userservice.model.UserEnrolledItem;
import com.learningpurpose.userservice.model.UserLearningMetrics;
import com.learningpurpose.userservice.repository.UserActivityLogRepository;
import com.learningpurpose.userservice.repository.UserDailyStudyRhythmRepository;
import com.learningpurpose.userservice.repository.UserEnrolledItemRepository;
import com.learningpurpose.userservice.repository.UserLearningMetricsRepository;
import com.learningpurpose.userservice.repository.UserRepository;
import com.learningpurpose.userservice.service.impl.DashboardServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class DashboardServiceImplTest {

    private UserRepository userRepo;
    private UserLearningMetricsRepository metricsRepo;
    private UserDailyStudyRhythmRepository rhythmRepo;
    private UserActivityLogRepository activityRepo;
    private UserEnrolledItemRepository enrolledRepo;
    private DashboardServiceImpl dashboardService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        userRepo = mock(UserRepository.class);
        metricsRepo = mock(UserLearningMetricsRepository.class);
        rhythmRepo = mock(UserDailyStudyRhythmRepository.class);
        activityRepo = mock(UserActivityLogRepository.class);
        enrolledRepo = mock(UserEnrolledItemRepository.class);

        dashboardService = new DashboardServiceImpl(
                userRepo, metricsRepo, rhythmRepo, activityRepo, enrolledRepo);

        sampleUser = User.builder()
                .id(1L)
                .username("john_doe")
                .email("john@example.com")
                .build();
    }

    @Test
    void getUserDashboard_Success_FullMetrics() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(sampleUser));

        UserLearningMetrics metrics = UserLearningMetrics.builder()
                .user(sampleUser)
                .currentStreakDays(7)
                .streakDiffLastWeek(3)
                .completedLessons(15)
                .lessonsCompletedThisWeek(4)
                .assessmentScore(new BigDecimal("92.50"))
                .personalBest(true)
                .studyTimeHours(new BigDecimal("5.00"))
                .weeklyGoalHours(new BigDecimal("6.00"))
                .build();
        when(metricsRepo.findByUserId(1L)).thenReturn(Optional.of(metrics));

        LocalDate now = LocalDate.now();
        LocalDate monday = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        UserDailyStudyRhythm rhythm = UserDailyStudyRhythm.builder()
                .user(sampleUser)
                .studyDate(monday)
                .hoursSpent(new BigDecimal("2.50"))
                .build();
        when(rhythmRepo.findByUserIdAndStudyDateBetweenOrderByStudyDateAsc(eq(1L), any(), any()))
                .thenReturn(List.of(rhythm));

        UserActivityLog activity = UserActivityLog.builder()
                .id(10L)
                .user(sampleUser)
                .title("Completed Exam")
                .subtitle("Scored 92.5% - Passed")
                .statusColor("green")
                .createdAt(Instant.now().minusSeconds(120))
                .build();
        when(activityRepo.findByUserIdOrderByCreatedAtDesc(eq(1L), any(Pageable.class)))
                .thenReturn(List.of(activity));

        UserEnrolledItem item = UserEnrolledItem.builder()
                .id(20L)
                .user(sampleUser)
                .itemType("QUIZ")
                .title("Java Concurrency")
                .progressPercentage(60)
                .status("IN PROGRESS")
                .actionText("Resume")
                .actionUrl("/quizzes/123")
                .build();
        when(enrolledRepo.findByUserIdOrderByUpdatedAtDesc(eq(1L), any(Pageable.class)))
                .thenReturn(List.of(item));

        DashboardResponseDto dto = dashboardService.getUserDashboard(1L);

        assertNotNull(dto);
        assertEquals("7", dto.getLearningStreak().getMainValue());
        assertEquals("+3 from last week", dto.getLearningStreak().getCaption());
        assertTrue(dto.getLearningStreak().getIsPositiveTrend());

        assertEquals("15", dto.getCompletedLessons().getMainValue());
        assertEquals("+4 this week", dto.getCompletedLessons().getCaption());

        assertEquals("92.5", dto.getAssessmentScore().getMainValue());
        assertEquals("Personal best", dto.getAssessmentScore().getCaption());

        assertEquals(1, dto.getRecentActivities().size());
        assertEquals("Completed Exam", dto.getRecentActivities().get(0).getTitle());
        assertEquals("2 minutes ago", dto.getRecentActivities().get(0).getTimeAgo());

        assertEquals(1, dto.getResumeItems().size());
        assertEquals("Java Concurrency", dto.getResumeItems().get(0).getTitle());

        // Next milestone for streak 7 should be 10 days
        assertEquals("10 day streak", dto.getNextMilestone().getTitle());
        assertEquals(7, dto.getNextMilestone().getCurrentProgress());
        assertEquals(10, dto.getNextMilestone().getMaxProgress());
    }

    @Test
    void getUserDashboard_NewUser_DefaultsSafely() {
        when(userRepo.findById(2L)).thenReturn(Optional.of(sampleUser));
        when(metricsRepo.findByUserId(2L)).thenReturn(Optional.empty());
        when(rhythmRepo.findByUserIdAndStudyDateBetweenOrderByStudyDateAsc(eq(2L), any(), any()))
                .thenReturn(Collections.emptyList());
        when(activityRepo.findByUserIdOrderByCreatedAtDesc(eq(2L), any(Pageable.class)))
                .thenReturn(Collections.emptyList());
        when(enrolledRepo.findByUserIdOrderByUpdatedAtDesc(eq(2L), any(Pageable.class)))
                .thenReturn(Collections.emptyList());

        DashboardResponseDto dto = dashboardService.getUserDashboard(2L);

        assertNotNull(dto);
        assertEquals("0", dto.getLearningStreak().getMainValue());
        assertEquals("Same as last week", dto.getLearningStreak().getCaption());
        assertEquals("0", dto.getCompletedLessons().getMainValue());
        assertEquals("0 this week", dto.getCompletedLessons().getCaption());
        assertEquals("0", dto.getAssessmentScore().getMainValue());
        assertEquals("0", dto.getStudyTime().getMainValue());
        assertEquals(0, dto.getWeeklyTarget().getTargetPercentage());
        assertEquals("5 day streak", dto.getNextMilestone().getTitle());
        assertTrue(dto.getRecentActivities().isEmpty());
        assertTrue(dto.getResumeItems().isEmpty());
        assertTrue(dto.getTodayFocus().getTitle().contains("Start your week strong"));
    }

    @Test
    void getUserDashboard_GoalReached_Celebrates() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(sampleUser));
        UserLearningMetrics metrics = UserLearningMetrics.builder()
                .user(sampleUser)
                .weeklyGoalHours(new BigDecimal("5.00"))
                .studyTimeHours(new BigDecimal("7.00"))
                .build();
        when(metricsRepo.findByUserId(1L)).thenReturn(Optional.of(metrics));

        DashboardResponseDto dto = dashboardService.getUserDashboard(1L);

        assertTrue(dto.getWeeklyTarget().getTargetPercentage() >= 100);
        assertTrue(dto.getTodayFocus().getTitle().contains("Goal reached"));
        assertEquals("Goal accomplished!", dto.getWeeklyTarget().getMotivationalText());
    }

    @Test
    void getUserDashboard_NegativeStreakDiff() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(sampleUser));
        UserLearningMetrics metrics = UserLearningMetrics.builder()
                .user(sampleUser)
                .currentStreakDays(2)
                .streakDiffLastWeek(-4)
                .build();
        when(metricsRepo.findByUserId(1L)).thenReturn(Optional.of(metrics));

        DashboardResponseDto dto = dashboardService.getUserDashboard(1L);

        assertEquals("-4 from last week", dto.getLearningStreak().getCaption());
        assertFalse(dto.getLearningStreak().getIsPositiveTrend());
    }

    @Test
    void getUserDashboard_NonExistentUser_ThrowsResourceNotFoundException() {
        when(userRepo.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> dashboardService.getUserDashboard(999L));
    }

    @Test
    void getUserDashboard_InvalidUserId_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> dashboardService.getUserDashboard(null));
        assertThrows(IllegalArgumentException.class, () -> dashboardService.getUserDashboard(0L));
        assertThrows(IllegalArgumentException.class, () -> dashboardService.getUserDashboard(-5L));
    }

    @Test
    void getDashboardForUser_ByUsernameAndEmail() {
        when(userRepo.findByUsername("john_doe")).thenReturn(Optional.of(sampleUser));
        when(userRepo.findById(1L)).thenReturn(Optional.of(sampleUser));

        DashboardResponseDto dto = dashboardService.getDashboardForUser("john_doe");
        assertNotNull(dto);

        when(userRepo.findByUsername("john@example.com")).thenReturn(Optional.empty());
        when(userRepo.findByEmail("john@example.com")).thenReturn(Optional.of(sampleUser));

        DashboardResponseDto dtoEmail = dashboardService.getDashboardForUser("john@example.com");
        assertNotNull(dtoEmail);

        assertThrows(IllegalArgumentException.class, () -> dashboardService.getDashboardForUser("   "));
    }

    @Test
    void logUserActivity_PersistsActivity() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(activityRepo.save(any(UserActivityLog.class))).thenAnswer(invocation -> {
            UserActivityLog arg = invocation.getArgument(0);
            arg.setId(101L);
            return arg;
        });

        CreateActivityRequest request = CreateActivityRequest.builder()
                .title("Read Article on Clean Architecture")
                .subtitle("Completed 15 min read")
                .actionType("ARTICLE_READ")
                .statusColor("purple")
                .build();

        ActivityDto result = dashboardService.logUserActivity(1L, request);

        assertNotNull(result);
        assertEquals("101", result.getId());
        assertEquals("Read Article on Clean Architecture", result.getTitle());
        assertEquals("Completed 15 min read", result.getSubtitle());
        assertEquals("Just now", result.getTimeAgo());
        assertEquals("purple", result.getStatusColor());

        verify(activityRepo).save(any(UserActivityLog.class));
    }

    @Test
    void logStudyTime_UpsertsRhythmAndMetrics() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(sampleUser));
        LocalDate today = LocalDate.now();
        when(rhythmRepo.findByUserIdAndStudyDate(1L, today)).thenReturn(Optional.empty());
        when(rhythmRepo.save(any(UserDailyStudyRhythm.class))).thenAnswer(i -> i.getArgument(0));

        when(metricsRepo.findByUserId(1L)).thenReturn(Optional.empty());
        when(metricsRepo.save(any(UserLearningMetrics.class))).thenAnswer(i -> i.getArgument(0));

        LogStudyTimeRequest request = LogStudyTimeRequest.builder()
                .hours(new BigDecimal("1.50"))
                .studyDate(today)
                .build();

        DailyBarDto result = dashboardService.logStudyTime(1L, request);

        assertNotNull(result);
        assertEquals(new BigDecimal("1.50"), result.getHours());

        ArgumentCaptor<UserDailyStudyRhythm> rhythmCaptor = ArgumentCaptor.forClass(UserDailyStudyRhythm.class);
        verify(rhythmRepo).save(rhythmCaptor.capture());
        assertEquals(new BigDecimal("1.50"), rhythmCaptor.getValue().getHoursSpent());

        ArgumentCaptor<UserLearningMetrics> metricsCaptor = ArgumentCaptor.forClass(UserLearningMetrics.class);
        verify(metricsRepo).save(metricsCaptor.capture());
        assertEquals(new BigDecimal("1.50"), metricsCaptor.getValue().getStudyTimeHours());
    }

    @Test
    void saveOrUpdateResumeItem_CreatesAndUpdates() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(enrolledRepo.findByUserIdAndItemTypeAndReferenceId(1L, "QUIZ", 55L)).thenReturn(Optional.empty());
        when(enrolledRepo.save(any(UserEnrolledItem.class))).thenAnswer(i -> i.getArgument(0));

        UpdateResumeItemRequest request = UpdateResumeItemRequest.builder()
                .itemType("QUIZ")
                .referenceId(55L)
                .title("Spring Boot Quiz")
                .description("Microservices module")
                .progressPercentage(75)
                .status("IN PROGRESS")
                .actionText("Resume")
                .actionUrl("/quizzes/55")
                .build();

        ResumeItemDto result = dashboardService.saveOrUpdateResumeItem(1L, request);

        assertNotNull(result);
        assertEquals("QUIZ", result.getType());
        assertEquals("Spring Boot Quiz", result.getTitle());
        assertEquals(75, result.getProgressPercentage());

        verify(enrolledRepo).save(any(UserEnrolledItem.class));
    }

    @Test
    void resolveUserId_NumericAndUsername() {
        when(userRepo.existsById(42L)).thenReturn(true);
        assertEquals(42L, dashboardService.resolveUserId("42"));

        when(userRepo.existsById(999L)).thenReturn(false);
        when(userRepo.findByUsername("999")).thenReturn(Optional.empty());
        when(userRepo.findByEmail("999")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> dashboardService.resolveUserId("999"));

        when(userRepo.findByUsername("john_doe")).thenReturn(Optional.of(sampleUser));
        assertEquals(1L, dashboardService.resolveUserId("john_doe"));

        assertThrows(IllegalArgumentException.class, () -> dashboardService.resolveUserId("   "));
    }
}
