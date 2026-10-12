package com.learningpurpose.userservice.service.impl;

import com.learningpurpose.userservice.dto.DashboardResponseDto;
import com.learningpurpose.userservice.dto.DashboardResponseDto.WeeklyTargetDto;
import com.learningpurpose.userservice.dto.DashboardResponseDto.MetricCardDto;
import com.learningpurpose.userservice.dto.DashboardResponseDto.ActivityDto;
import com.learningpurpose.userservice.dto.DashboardResponseDto.ResumeItemDto;
import com.learningpurpose.userservice.dto.DashboardResponseDto.TodayFocusDto;
import com.learningpurpose.userservice.dto.DashboardResponseDto.StudyRhythmDto;
import com.learningpurpose.userservice.dto.DashboardResponseDto.RecommendedNextDto;
import com.learningpurpose.userservice.dto.DashboardResponseDto.DailyBarDto;
import com.learningpurpose.userservice.dto.DashboardResponseDto.NextMilestoneDto;
import com.learningpurpose.userservice.model.UserDailyStudyRhythm;
import com.learningpurpose.userservice.model.UserLearningMetrics;
import com.learningpurpose.userservice.repository.UserActivityLogRepository;
import com.learningpurpose.userservice.repository.UserDailyStudyRhythmRepository;
import com.learningpurpose.userservice.repository.UserEnrolledItemRepository;
import com.learningpurpose.userservice.repository.UserLearningMetricsRepository;
import com.learningpurpose.userservice.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final UserLearningMetricsRepository metricsRepo;
    private final UserDailyStudyRhythmRepository rhythmRepo;
    private final UserActivityLogRepository activityRepo;
    private final UserEnrolledItemRepository enrolledRepo;

    @Override
    @Transactional(readOnly = true)
    public DashboardResponseDto getUserDashboard(Long userId) {
        UserLearningMetrics metrics = metricsRepo.findByUserId(userId)
                .orElseGet(() -> UserLearningMetrics.builder()
                        .currentStreakDays(7)
                        .streakDiffLastWeek(2)
                        .completedLessons(24)
                        .lessonsCompletedThisWeek(6)
                        .assessmentScore(new BigDecimal("86.00"))
                        .personalBest(true)
                        .studyTimeHours(new BigDecimal("4.50"))
                        .weeklyGoalHours(new BigDecimal("6.00"))
                        .build());

        // 1. Weekly Rhythm calculation
        LocalDate now = LocalDate.now();
        LocalDate startOfWeek = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        List<UserDailyStudyRhythm> recordedRhythms = rhythmRepo.findByUserIdAndStudyDateBetweenOrderByStudyDateAsc(userId, startOfWeek, endOfWeek);
        Map<LocalDate, BigDecimal> dateMap = recordedRhythms.stream()
                .collect(Collectors.toMap(UserDailyStudyRhythm::getStudyDate, UserDailyStudyRhythm::getHoursSpent, (a, b) -> a));

        List<DailyBarDto> dailyBars = new ArrayList<>();
        BigDecimal totalRhythmHours = BigDecimal.ZERO;
        String[] dayLabels = {"M", "T", "W", "T", "F", "S", "S"};

        for (int i = 0; i < 7; i++) {
            LocalDate date = startOfWeek.plusDays(i);
            BigDecimal hours = dateMap.getOrDefault(date, BigDecimal.ZERO);
            totalRhythmHours = totalRhythmHours.add(hours);
            dailyBars.add(DailyBarDto.builder()
                    .dayOfWeek(dayLabels[i])
                    .hours(hours)
                    .build());
        }

        // 2. Weekly Target calculation
        int targetPercentage = 0;
        if (metrics.getWeeklyGoalHours().compareTo(BigDecimal.ZERO) > 0) {
            targetPercentage = metrics.getStudyTimeHours()
                    .divide(metrics.getWeeklyGoalHours(), 2, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .intValue();
        }

        // 3. Activity Feed mapping
        List<ActivityDto> activities = activityRepo.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, 5))
                .stream()
                .map(item -> ActivityDto.builder()
                        .id(String.valueOf(item.getId()))
                        .title(item.getTitle())
                        .subtitle(item.getSubtitle())
                        .timeAgo("Recent")
                        .statusColor(item.getStatusColor())
                        .build())
                .collect(Collectors.toList());

        // 4. Resume Items mapping
        List<ResumeItemDto> resumeItems = enrolledRepo.findByUserIdOrderByUpdatedAtDesc(userId, PageRequest.of(0, 4))
                .stream()
                .map(item -> ResumeItemDto.builder()
                        .type(item.getItemType())
                        .status(item.getStatus())
                        .title(item.getTitle())
                        .description(item.getDescription())
                        .progressPercentage(item.getProgressPercentage())
                        .actionText(item.getActionText())
                        .actionUrl(item.getActionUrl())
                        .build())
                .collect(Collectors.toList());

        return DashboardResponseDto.builder()
                .todayFocus(TodayFocusDto.builder()
                        .title("Finish strong with one meaningful session.")
                        .subtitle("You are " + targetPercentage + "% through your weekly goal. A 45-minute study session keeps your streak alive.")
                        .actionUrl("/learning/continue")
                        .build())
                .studyRhythm(StudyRhythmDto.builder()
                        .totalHoursThisWeek(totalRhythmHours.compareTo(BigDecimal.ZERO) > 0 ? totalRhythmHours : metrics.getStudyTimeHours())
                        .dailyHours(dailyBars)
                        .build())
                .learningStreak(MetricCardDto.builder()
                        .mainValue(String.valueOf(metrics.getCurrentStreakDays()))
                        .unit("days")
                        .caption("+" + metrics.getStreakDiffLastWeek() + " from last week")
                        .isPositiveTrend(metrics.getStreakDiffLastWeek() >= 0)
                        .build())
                .completedLessons(MetricCardDto.builder()
                        .mainValue(String.valueOf(metrics.getCompletedLessons()))
                        .unit("lessons")
                        .caption("+" + metrics.getLessonsCompletedThisWeek() + " this week")
                        .isPositiveTrend(true)
                        .build())
                .assessmentScore(MetricCardDto.builder()
                        .mainValue(metrics.getAssessmentScore().stripTrailingZeros().toPlainString())
                        .unit("%")
                        .caption(metrics.isPersonalBest() ? "Personal best" : "Average score")
                        .isPositiveTrend(metrics.isPersonalBest())
                        .build())
                .studyTime(MetricCardDto.builder()
                        .mainValue(metrics.getStudyTimeHours().stripTrailingZeros().toPlainString())
                        .unit("hrs")
                        .caption("of " + metrics.getWeeklyGoalHours().stripTrailingZeros().toPlainString() + " hr weekly goal")
                        .isPositiveTrend(null)
                        .build())
                .weeklyTarget(WeeklyTargetDto.builder()
                        .targetPercentage(targetPercentage)
                        .completedHours(metrics.getStudyTimeHours())
                        .goalHours(metrics.getWeeklyGoalHours())
                        .motivationalText("Keep your momentum")
                        .recommendedNext(RecommendedNextDto.builder()
                                .title("Complete the data structures assessment")
                                .linkUrl("/assessments/data-structures")
                                .build())
                        .build())
                .recentActivities(activities)
                .resumeItems(resumeItems)
                .nextMilestone(NextMilestoneDto.builder()
                        .title("10 day streak")
                        .description("Keep learning for " + Math.max(0, 10 - metrics.getCurrentStreakDays()) + " more days to unlock your next achievement.")
                        .currentProgress(metrics.getCurrentStreakDays())
                        .maxProgress(10)
                        .build())
                .build();
    }
}
