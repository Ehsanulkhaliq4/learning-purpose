package com.learningpurpose.notification_service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExamSubmittedEvent {
    private Long quizId;
    private String quizTitle;
    private String username;
    private String userEmail;
    private double marksScored;
    private double maxMarks;
    private double percentage;
    private boolean passed;
    private Instant submittedAt;
}
