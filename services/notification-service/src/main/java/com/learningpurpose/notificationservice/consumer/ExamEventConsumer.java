package com.learningpurpose.notificationservice.consumer;

import com.learningpurpose.notificationservice.event.ExamSubmittedEvent;
import com.learningpurpose.notificationservice.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExamEventConsumer {
    private final EmailService emailService;

    @KafkaListener(topics = "exam-submitted-topic", groupId = "notification-service-group")
    public void handleExamSubmitted(ExamSubmittedEvent event) {
        log.info("Received ExamSubmittedEvent for user: {}", event.getUserEmail());
        emailService.sendExamResultEmail(
                event.getUserEmail(),
                event.getUsername(),
                event.getQuizTitle(),
                event.getMarksScored(),
                event.getMaxMarks(),
                event.isPassed()
        );
    }
}
