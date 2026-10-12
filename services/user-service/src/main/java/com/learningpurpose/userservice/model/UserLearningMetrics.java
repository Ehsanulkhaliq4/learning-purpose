package com.learningpurpose.userservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "user_learning_metrics")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLearningMetrics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "current_streak_days", nullable = false)
    private int currentStreakDays;

    @Column(name = "streak_diff_last_week", nullable = false)
    private int streakDiffLastWeek;

    @Column(name = "completed_lessons", nullable = false)
    private int completedLessons;

    @Column(name = "lessons_completed_this_week", nullable = false)
    private int lessonsCompletedThisWeek;

    @Column(name = "assessment_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal assessmentScore;

    @Column(name = "is_personal_best", nullable = false)
    private boolean personalBest;

    @Column(name = "study_time_hours", nullable = false, precision = 4, scale = 2)
    private BigDecimal studyTimeHours;

    @Column(name = "weekly_goal_hours", nullable = false, precision = 4, scale = 2)
    private BigDecimal weeklyGoalHours;

    @Column(name = "updated_at")
    private Instant updatedAt;
}
