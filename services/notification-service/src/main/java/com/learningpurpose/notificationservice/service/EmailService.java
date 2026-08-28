package com.learningpurpose.notificationservice.service;

public interface EmailService {
    void sendWelcomeEmail(String toEmail, String name);
    void sendOtpEmail(String toEmail, String otp);
    void sendExamResultEmail(String toEmail, String username, String quizTitle, double marks, double maxMarks, boolean passed);
}
