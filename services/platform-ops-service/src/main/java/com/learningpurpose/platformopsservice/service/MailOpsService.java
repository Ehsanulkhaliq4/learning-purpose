package com.learningpurpose.platformopsservice.service;

import jakarta.mail.Transport;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailOpsService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.host}")
    private String mailHost;

    @Value("${spring.mail.port}")
    private int mailPort;


    public MailStatusReport testConnection() {
        long start = System.currentTimeMillis();
        try {
            if (mailSender instanceof JavaMailSenderImpl impl) {
                try (Transport transport = impl.getSession().getTransport("smtp")) {
                    transport.connect(mailHost, mailPort, "", impl.getPassword());
                    long latency = System.currentTimeMillis() - start;
                    return new MailStatusReport(true, "SMTP Server is reachable and authenticated.", latency, mailHost, mailPort);
                }
            }
            return new MailStatusReport(true, "MailSender initialized successfully.", 0, mailHost, mailPort);
        } catch (Exception e) {
            log.error("SMTP Probe failure: {}", e.getMessage());
            long latency = System.currentTimeMillis() - start;
            return new MailStatusReport(false, "SMTP Check Failed: " + e.getMessage(), latency, mailHost, mailPort);
        }
    }

    public MailStatusReport sendDiagnosticProbe(String recipient) {
        long start = System.currentTimeMillis();
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom("");
            helper.setTo(recipient);
            helper.setSubject("Diagnostic SMTP Probe - " + Instant.now());
            helper.setText("<h3>Platform Ops Diagnostic Probe</h3><p>SMTP subsystem connectivity test successful.</p>", true);
            helper.setSentDate(new Date());

            mailSender.send(message);
            long elapsed = System.currentTimeMillis() - start;
            return new MailStatusReport(true, "Test email delivered to " + recipient, elapsed, mailHost, mailPort);
        } catch (Exception e) {
            log.error("Failed to send diagnostic email to {}", recipient, e);
            long elapsed = System.currentTimeMillis() - start;
            return new MailStatusReport(false, "Dispatch error: " + e.getMessage(), elapsed, mailHost, mailPort);
        }
    }

    public record MailStatusReport(boolean connected, String message, long latencyMs, String host, int port) {}
}
