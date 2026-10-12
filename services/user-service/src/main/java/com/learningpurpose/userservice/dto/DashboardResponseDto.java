package com.learningpurpose.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardResponseDto {

    private TodayFocusDto todayFocus;
    private StudyRhythmDto studyRhythm;
    private MetricCardDto learningStreak;
    private MetricCardDto completedLessons;
    private MetricCardDto assessmentScore;
    private MetricCardDto studyTime;
    private WeeklyTargetDto weeklyTarget;
    private List<ActivityDto> recentActivities;
    private List<ResumeItemDto> resumeItems;
    private NextMilestoneDto nextMilestone;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TodayFocusDto {
        private String title;
        private String subtitle;
        private String actionUrl;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StudyRhythmDto {
        private BigDecimal totalHoursThisWeek;
        private List<DailyBarDto> dailyHours;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DailyBarDto {
        private String dayOfWeek;
        private BigDecimal hours;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MetricCardDto {
        private String mainValue;
        private String unit;
        private String caption;
        private Boolean isPositiveTrend;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WeeklyTargetDto {
        private int targetPercentage;
        private BigDecimal completedHours;
        private BigDecimal goalHours;
        private String motivationalText;
        private RecommendedNextDto recommendedNext;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RecommendedNextDto {
        private String title;
        private String linkUrl;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ActivityDto {
        private String id;
        private String title;
        private String subtitle;
        private String timeAgo;
        private String statusColor;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ResumeItemDto {
        private String type;
        private String status;
        private String title;
        private String description;
        private int progressPercentage;
        private String actionText;
        private String actionUrl;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class NextMilestoneDto {
        private String title;
        private String description;
        private int currentProgress;
        private int maxProgress;
    }
}
