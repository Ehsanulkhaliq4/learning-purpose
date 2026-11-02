package com.learningpurpose.userservice.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learningpurpose.userservice.model.User;
import com.learningpurpose.userservice.model.UserActivityLog;
import com.learningpurpose.userservice.model.UserDailyStudyRhythm;
import com.learningpurpose.userservice.model.UserLearningMetrics;
import com.learningpurpose.userservice.repository.UserActivityLogRepository;
import com.learningpurpose.userservice.repository.UserDailyStudyRhythmRepository;
import com.learningpurpose.userservice.repository.UserLearningMetricsRepository;
import com.learningpurpose.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExamActivityConsumer {

    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final UserActivityLogRepository activityRepository;
    private final UserLearningMetricsRepository metricsRepository;
    private final UserDailyStudyRhythmRepository rhythmRepository;

    private static final BigDecimal EXAM_STUDY_HOURS = new BigDecimal("0.50");

    @KafkaListener(topics = "exam-submitted-topic")
    @Transactional
    public void onExamSubmitted(String payload) {
        if (payload == null || payload.isBlank()) {
            log.warn("Received empty exam submission payload");
            return;
        }

        try {
            JsonNode event = objectMapper.readTree(payload);
            String email = event.path("userEmail").asText("").trim().toLowerCase();
            String username = event.path("username").asText("").trim();

            User user = null;
            if (!email.isEmpty()) {
                user = userRepository.findByEmail(email).orElse(null);
            }
            if (user == null && !username.isEmpty()) {
                user = userRepository.findByUsername(username).orElse(null);
            }

            if (user == null) {
                log.warn("Ignoring exam submission for unknown user (email='{}', username='{}')", email, username);
                return;
            }

            String quizTitle = event.path("quizTitle").asText("Exam").trim();
            if (quizTitle.isEmpty()) {
                quizTitle = "Exam";
            }

            double rawPercentage = event.path("percentage").asDouble(0);
            if (Double.isNaN(rawPercentage) || Double.isInfinite(rawPercentage)) {
                rawPercentage = 0.0;
            }
            BigDecimal percentage = BigDecimal.valueOf(rawPercentage)
                    .max(BigDecimal.ZERO)
                    .min(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);

            boolean passed = event.path("passed").asBoolean(false);

            Instant submittedAt;
            if (event.hasNonNull("submittedAt")) {
                try {
                    submittedAt = Instant.parse(event.get("submittedAt").asText());
                } catch (Exception ex) {
                    submittedAt = Instant.now();
                }
            } else {
                submittedAt = Instant.now();
            }

            final User finalUser = user;
            final Instant finalSubmittedAt = submittedAt;

            // 1. Insert into user activity log
            activityRepository.save(UserActivityLog.builder()
                    .user(finalUser)
                    .title("Completed " + quizTitle)
                    .subtitle("Scored " + percentage.stripTrailingZeros().toPlainString() + "% - " + (passed ? "Passed" : "Not passed"))
                    .actionType("EXAM_SUBMITTED")
                    .statusColor(passed ? "green" : "red")
                    .createdAt(finalSubmittedAt)
                    .build());

            // 2. Update today's study rhythm with exam study time
            LocalDate today = LocalDate.now();
            UserDailyStudyRhythm rhythm = rhythmRepository.findByUserIdAndStudyDate(finalUser.getId(), today)
                    .orElseGet(() -> UserDailyStudyRhythm.builder()
                            .user(finalUser)
                            .studyDate(today)
                            .hoursSpent(BigDecimal.ZERO)
                            .createdAt(finalSubmittedAt)
                            .build());
            BigDecimal currentHours = rhythm.getHoursSpent() != null ? rhythm.getHoursSpent() : BigDecimal.ZERO;
            rhythm.setHoursSpent(currentHours.add(EXAM_STUDY_HOURS));
            rhythmRepository.save(rhythm);

            // 3. Update learning metrics (score, lessons completed, study hours)
            UserLearningMetrics metrics = metricsRepository.findByUserId(finalUser.getId())
                    .orElseGet(() -> UserLearningMetrics.builder()
                            .user(finalUser)
                            .currentStreakDays(1)
                            .streakDiffLastWeek(0)
                            .completedLessons(0)
                            .lessonsCompletedThisWeek(0)
                            .assessmentScore(BigDecimal.ZERO)
                            .personalBest(false)
                            .studyTimeHours(BigDecimal.ZERO)
                            .weeklyGoalHours(new BigDecimal("6.00"))
                            .build());

            BigDecimal currentScore = metrics.getAssessmentScore() != null ? metrics.getAssessmentScore() : BigDecimal.ZERO;
            boolean personalBest = percentage.compareTo(currentScore) > 0;
            if (personalBest || currentScore.compareTo(BigDecimal.ZERO) == 0) {
                metrics.setAssessmentScore(percentage);
            }
            metrics.setPersonalBest(metrics.isPersonalBest() || personalBest);
            metrics.setCompletedLessons(metrics.getCompletedLessons() + 1);
            metrics.setLessonsCompletedThisWeek(metrics.getLessonsCompletedThisWeek() + 1);

            BigDecimal currentStudyHours = metrics.getStudyTimeHours() != null ? metrics.getStudyTimeHours() : BigDecimal.ZERO;
            metrics.setStudyTimeHours(currentStudyHours.add(EXAM_STUDY_HOURS));
            metrics.setUpdatedAt(submittedAt);

            metricsRepository.save(metrics);

            log.info("Successfully recorded real-time exam activity for user {} (quiz: '{}', score: {}%)",
                    user.getUsername(), quizTitle, percentage);

        } catch (Exception e) {
            log.error("Failed to process exam submitted event payload: {}", payload, e);
        }
    }
}