package com.learningpurpose.notificationservice.consumer;

import com.learningpurpose.notificationservice.event.OtpRequestedEvent;
import com.learningpurpose.notificationservice.event.UserRegisteredEvent;
import com.learningpurpose.notificationservice.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthEventConsumer {
    private final EmailService emailService;

    @KafkaListener(topics = "user-registered-topic", groupId = "notification-service-group")
    public void handleUserRegistered(UserRegisteredEvent event) {
        log.info("Received UserRegisteredEvent for email: {}", event.getEmail());
        emailService.sendWelcomeEmail(event.getEmail(), event.getFirstName());
    }

    @KafkaListener(topics = "otp-requested-topic", groupId = "notification-service-group")
    public void handleOtpRequested(OtpRequestedEvent event) {
        log.info("Received OtpRequestedEvent for email: {}", event.getEmail());
        emailService.sendOtpEmail(event.getEmail(), event.getOtp());
    }
}
