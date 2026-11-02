package com.learningpurpose.userservice.service.impl;

import com.learningpurpose.userservice.dto.CreateActivityRequest;
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
import com.learningpurpose.userservice.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final UserRepository userRepo;
    private final UserLearningMetricsRepository metricsRepo;
    private final UserDailyStudyRhythmRepository rhythmRepo;
    private final UserActivityLogRepository activityRepo;
    private final UserEnrolledItemRepository enrolledRepo;

    private static final int[] MILESTONE_TIERS = {5, 10, 20, 30, 50, 75, 100, 150, 200, 365};

    @Override
    @Transactional(readOnly = true)
    public DashboardResponseDto getUserDashboard(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID must be a positive number");
        }

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        UserLearningMetrics metrics = metricsRepo.findByUserId(userId)
                .orElseGet(() -> UserLearningMetrics.builder()
                        .user(user)
                        .currentStreakDays(0)
                        .streakDiffLastWeek(0)
                        .completedLessons(0)
                        .lessonsCompletedThisWeek(0)
                        .assessmentScore(BigDecimal.ZERO)
                        .personalBest(false)
                        .studyTimeHours(BigDecimal.ZERO)
                        .weeklyGoalHours(new BigDecimal("6.00"))
                        .build());

        // 1. Weekly Rhythm calculation
        LocalDate now = LocalDate.now();
        LocalDate startOfWeek = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        List<UserDailyStudyRhythm> recordedRhythms = rhythmRepo.findByUserIdAndStudyDateBetweenOrderByStudyDateAsc(userId, startOfWeek, endOfWeek);
        Map<LocalDate, BigDecimal> dateMap = new HashMap<>();
        for (UserDailyStudyRhythm rhythm : recordedRhythms) {
            if (rhythm.getStudyDate() != null) {
                dateMap.merge(rhythm.getStudyDate(), safeBigDecimal(rhythm.getHoursSpent(), BigDecimal.ZERO), BigDecimal::add);
            }
        }

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
        BigDecimal weeklyGoal = safeBigDecimal(metrics.getWeeklyGoalHours(), new BigDecimal("6.00"));
        BigDecimal weeklyStudyHours = totalRhythmHours.compareTo(BigDecimal.ZERO) > 0
                ? totalRhythmHours
                : safeBigDecimal(metrics.getStudyTimeHours(), BigDecimal.ZERO);

        int targetPercentage = 0;
        if (weeklyGoal.compareTo(BigDecimal.ZERO) > 0) {
            targetPercentage = weeklyStudyHours
                    .divide(weeklyGoal, 2, RoundingMode.HALF_UP)
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
                        .timeAgo(formatTimeAgo(item.getCreatedAt()))
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

        // 5. Dynamic Milestone
        int currentStreak = Math.max(0, metrics.getCurrentStreakDays());
        int nextMilestoneGoal = computeNextMilestone(currentStreak);
        int remainingDays = Math.max(0, nextMilestoneGoal - currentStreak);

        // 6. Focus message
        String focusTitle;
        String focusSubtitle;
        if (targetPercentage >= 100) {
            focusTitle = "Goal reached! Amazing momentum this week.";
            focusSubtitle = "You've logged " + targetPercentage + "% of your weekly goal. Keep your streak alive with another session.";
        } else if (targetPercentage == 0) {
            focusTitle = "Start your week strong with a study session.";
            focusSubtitle = "You have 0% logged toward your " + weeklyGoal.stripTrailingZeros().toPlainString() + " hr weekly goal. Kick off your first lesson today!";
        } else {
            focusTitle = "Finish strong with one meaningful session.";
            focusSubtitle = "You are " + targetPercentage + "% through your weekly goal. A 45-minute study session keeps your streak alive.";
        }

        int streakDiff = metrics.getStreakDiffLastWeek();
        String streakCaption = streakDiff > 0
                ? "+" + streakDiff + " from last week"
                : (streakDiff < 0 ? streakDiff + " from last week" : "Same as last week");

        int lessonsThisWeek = metrics.getLessonsCompletedThisWeek();
        String lessonsCaption = lessonsThisWeek > 0
                ? "+" + lessonsThisWeek + " this week"
                : "0 this week";

        BigDecimal assessmentScore = safeBigDecimal(metrics.getAssessmentScore(), BigDecimal.ZERO);

        return DashboardResponseDto.builder()
                .todayFocus(TodayFocusDto.builder()
                        .title(focusTitle)
                        .subtitle(focusSubtitle)
                        .actionUrl("/quizzes")
                        .build())
                .studyRhythm(StudyRhythmDto.builder()
                        .totalHoursThisWeek(weeklyStudyHours)
                        .dailyHours(dailyBars)
                        .build())
                .learningStreak(MetricCardDto.builder()
                        .mainValue(String.valueOf(currentStreak))
                        .unit("days")
                        .caption(streakCaption)
                        .isPositiveTrend(streakDiff >= 0)
                        .build())
                .completedLessons(MetricCardDto.builder()
                        .mainValue(String.valueOf(metrics.getCompletedLessons()))
                        .unit("lessons")
                        .caption(lessonsCaption)
                        .isPositiveTrend(true)
                        .build())
                .assessmentScore(MetricCardDto.builder()
                        .mainValue(assessmentScore.stripTrailingZeros().toPlainString())
                        .unit("%")
                        .caption(metrics.isPersonalBest() ? "Personal best" : "Average score")
                        .isPositiveTrend(metrics.isPersonalBest())
                        .build())
                .studyTime(MetricCardDto.builder()
                        .mainValue(weeklyStudyHours.stripTrailingZeros().toPlainString())
                        .unit("hrs")
                        .caption("of " + weeklyGoal.stripTrailingZeros().toPlainString() + " hr weekly goal")
                        .isPositiveTrend(targetPercentage >= 100)
                        .build())
                .weeklyTarget(WeeklyTargetDto.builder()
                        .targetPercentage(targetPercentage)
                        .completedHours(weeklyStudyHours)
                        .goalHours(weeklyGoal)
                        .motivationalText(targetPercentage >= 100 ? "Goal accomplished!" : "Keep your momentum")
                        .recommendedNext(RecommendedNextDto.builder()
                                .title("Complete the data structures assessment")
                                .linkUrl("/quizzes")
                                .build())
                        .build())
                .recentActivities(activities)
                .resumeItems(resumeItems)
                .nextMilestone(NextMilestoneDto.builder()
                        .title(nextMilestoneGoal + " day streak")
                        .description("Keep learning for " + remainingDays + (remainingDays == 1 ? " more day" : " more days") + " to unlock your next achievement.")
                        .currentProgress(currentStreak)
                        .maxProgress(nextMilestoneGoal)
                        .build())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardResponseDto getDashboardForUser(String usernameOrEmail) {
        if (usernameOrEmail == null || usernameOrEmail.isBlank()) {
            throw new IllegalArgumentException("Username or email must not be blank");
        }
        String identifier = usernameOrEmail.trim();
        User user = userRepo.findByUsername(identifier)
                .or(() -> userRepo.findByEmail(identifier.toLowerCase()))
                .orElseThrow(() -> new ResourceNotFoundException("User not found with identifier: " + identifier));
        return getUserDashboard(user.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Long resolveUserId(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            throw new IllegalArgumentException("User identifier must not be blank");
        }
        String trimmed = identifier.trim();
        if (trimmed.matches("^\\d+$")) {
            Long id = Long.parseLong(trimmed);
            if (userRepo.existsById(id)) {
                return id;
            }
        }
        User user = userRepo.findByUsername(trimmed)
                .or(() -> userRepo.findByEmail(trimmed.toLowerCase()))
                .orElseThrow(() -> new ResourceNotFoundException("User not found with identifier: " + trimmed));
        return user.getId();
    }

    @Override
    @Transactional
    public ActivityDto logUserActivity(Long userId, CreateActivityRequest request) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID must be a positive number");
        }
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        UserActivityLog saved = activityRepo.save(UserActivityLog.builder()
                .user(user)
                .title(request.getTitle() != null ? request.getTitle().trim() : "Activity")
                .subtitle(request.getSubtitle() != null ? request.getSubtitle().trim() : "")
                .actionType(request.getActionType() != null && !request.getActionType().isBlank() ? request.getActionType().trim() : "GENERAL")
                .statusColor(request.getStatusColor() != null && !request.getStatusColor().isBlank() ? request.getStatusColor().trim() : "blue")
                .createdAt(Instant.now())
                .build());

        return ActivityDto.builder()
                .id(String.valueOf(saved.getId()))
                .title(saved.getTitle())
                .subtitle(saved.getSubtitle())
                .timeAgo("Just now")
                .statusColor(saved.getStatusColor())
                .build();
    }

    @Override
    @Transactional
    public DailyBarDto logStudyTime(Long userId, LogStudyTimeRequest request) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID must be a positive number");
        }
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        LocalDate studyDate = request.getStudyDate() != null ? request.getStudyDate() : LocalDate.now();
        BigDecimal addedHours = safeBigDecimal(request.getHours(), BigDecimal.ZERO);

        UserDailyStudyRhythm rhythm = rhythmRepo.findByUserIdAndStudyDate(userId, studyDate)
                .orElseGet(() -> UserDailyStudyRhythm.builder()
                        .user(user)
                        .studyDate(studyDate)
                        .hoursSpent(BigDecimal.ZERO)
                        .createdAt(Instant.now())
                        .build());

        rhythm.setHoursSpent(safeBigDecimal(rhythm.getHoursSpent(), BigDecimal.ZERO).add(addedHours));
        rhythmRepo.save(rhythm);

        // Also update total study time in metrics
        UserLearningMetrics metrics = metricsRepo.findByUserId(userId)
                .orElseGet(() -> UserLearningMetrics.builder()
                        .user(user)
                        .currentStreakDays(1)
                        .streakDiffLastWeek(0)
                        .completedLessons(0)
                        .lessonsCompletedThisWeek(0)
                        .assessmentScore(BigDecimal.ZERO)
                        .personalBest(false)
                        .studyTimeHours(BigDecimal.ZERO)
                        .weeklyGoalHours(new BigDecimal("6.00"))
                        .build());

        metrics.setStudyTimeHours(safeBigDecimal(metrics.getStudyTimeHours(), BigDecimal.ZERO).add(addedHours));
        metrics.setUpdatedAt(Instant.now());
        metricsRepo.save(metrics);

        String[] dayLabels = {"M", "T", "W", "T", "F", "S", "S"};
        int dayIndex = studyDate.getDayOfWeek().getValue() - 1;

        return DailyBarDto.builder()
                .dayOfWeek(dayLabels[dayIndex])
                .hours(rhythm.getHoursSpent())
                .build();
    }

    @Override
    @Transactional
    public ResumeItemDto saveOrUpdateResumeItem(Long userId, UpdateResumeItemRequest request) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID must be a positive number");
        }
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        UserEnrolledItem item = null;
        if (request.getReferenceId() != null) {
            item = enrolledRepo.findByUserIdAndItemTypeAndReferenceId(userId, request.getItemType(), request.getReferenceId())
                    .orElse(null);
        }

        if (item == null) {
            item = UserEnrolledItem.builder()
                    .user(user)
                    .itemType(request.getItemType())
                    .referenceId(request.getReferenceId())
                    .build();
        }

        item.setTitle(request.getTitle());
        item.setDescription(request.getDescription());
        item.setProgressPercentage(Math.max(0, Math.min(100, request.getProgressPercentage())));
        item.setStatus(request.getStatus() != null && !request.getStatus().isBlank() ? request.getStatus() : "IN PROGRESS");
        item.setActionText(request.getActionText() != null && !request.getActionText().isBlank() ? request.getActionText() : "Continue ->");
        item.setActionUrl(request.getActionUrl() != null && !request.getActionUrl().isBlank() ? request.getActionUrl() : "/quizzes");
        item.setUpdatedAt(Instant.now());

        UserEnrolledItem saved = enrolledRepo.save(item);

        return ResumeItemDto.builder()
                .type(saved.getItemType())
                .status(saved.getStatus())
                .title(saved.getTitle())
                .description(saved.getDescription())
                .progressPercentage(saved.getProgressPercentage())
                .actionText(saved.getActionText())
                .actionUrl(saved.getActionUrl())
                .build();
    }

    private int computeNextMilestone(int currentStreak) {
        for (int tier : MILESTONE_TIERS) {
            if (tier > currentStreak) {
                return tier;
            }
        }
        return currentStreak + 50;
    }

    private BigDecimal safeBigDecimal(BigDecimal value, BigDecimal defaultValue) {
        return value != null ? value : defaultValue;
    }

    private String formatTimeAgo(Instant createdAt) {
        if (createdAt == null) {
            return "Just now";
        }

        Duration age = Duration.between(createdAt, Instant.now());
        if (age.isNegative() || age.getSeconds() < 60) {
            return "Just now";
        }
        if (age.toMinutes() < 60) {
            long minutes = age.toMinutes();
            return minutes + (minutes == 1 ? " minute ago" : " minutes ago");
        }
        if (age.toHours() < 24) {
            long hours = age.toHours();
            return hours + (hours == 1 ? " hour ago" : " hours ago");
        }
        if (age.toDays() < 7) {
            long days = age.toDays();
            return days + (days == 1 ? " day ago" : " days ago");
        }
        if (age.toDays() < 30) {
            long weeks = age.toDays() / 7;
            return weeks + (weeks == 1 ? " week ago" : " weeks ago");
        }
        long months = age.toDays() / 30;
        return months + (months == 1 ? " month ago" : " months ago");
    }
}
