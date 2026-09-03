package com.learningpurpose.notificationservice.consumer;

import com.learningpurpose.notificationservice.event.ExamSubmittedEvent;
import com.learningpurpose.notificationservice.event.OtpRequestedEvent;
import com.learningpurpose.notificationservice.event.UserRegisteredEvent;
import com.learningpurpose.notificationservice.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthNotificationConsumer {

    private final EmailService emailService;

    @KafkaListener(topics = "user-registered-topic", containerFactory = "kafkaListenerContainerFactory")
    public void onUserRegistered(@Payload UserRegisteredEvent event) {
        log.info("Successfully received UserRegisteredEvent: {}", event.getEmail());
        emailService.sendWelcomeEmail(event.getEmail(), event.getFirstName());
    }

    @KafkaListener(topics = "otp-requested-topic", containerFactory = "kafkaListenerContainerFactory")
    public void onOtpRequested(@Payload OtpRequestedEvent event) {
        log.info("Successfully received OtpRequestedEvent: {}", event.getEmail());
        emailService.sendOtpEmail(event.getEmail(), event.getOtp());
    }

    @KafkaListener(topics = "exam-submitted-topic", containerFactory = "kafkaListenerContainerFactory")
    public void onExamSubmitted(@Payload ExamSubmittedEvent event) {
        log.info("Successfully received ExamSubmittedEvent: {}", event.getUserEmail());
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