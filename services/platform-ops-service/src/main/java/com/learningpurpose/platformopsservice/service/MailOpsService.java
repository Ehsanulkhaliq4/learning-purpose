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
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailOpsService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.host}")
    private String mailHost;

    @Value("${spring.mail.port}")
    private int mailPort;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${spring.mail.password:}")
    private String mailPassword;

    private static final String FROM_ADDRESS = "noreply@learning-purpose.local";

    /* ─────────────── probes ─────────────── */

    public MailStatusReport testConnection() {
        long start = System.currentTimeMillis();
        try {
            if (mailSender instanceof JavaMailSenderImpl impl) {
                try (Transport transport = impl.getSession().getTransport("smtp")) {
                    transport.connect(mailHost, mailPort, mailUsername, mailPassword);
                    long latency = System.currentTimeMillis() - start;
                    return new MailStatusReport(true,
                            "SMTP Server is reachable and authenticated.",
                            latency, mailHost, mailPort);
                }
            }
            return new MailStatusReport(true,
                    "MailSender initialized successfully.", 0, mailHost, mailPort);
        } catch (Exception e) {
            log.error("SMTP Probe failure: {}", e.getMessage());
            long latency = System.currentTimeMillis() - start;
            return new MailStatusReport(false,
                    "SMTP Check Failed: " + e.getMessage(), latency, mailHost, mailPort);
        }
    }

    public MailStatusReport sendDiagnosticProbe(String recipient) {
        long start = System.currentTimeMillis();
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(FROM_ADDRESS);
            helper.setTo(recipient);
            helper.setSubject("Diagnostic SMTP Probe - " + Instant.now());
            helper.setText("""
                    <h3>Platform Ops Diagnostic Probe</h3>
                    <p>SMTP subsystem connectivity test successful.</p>
                    """, true);
            helper.setSentDate(new Date());

            mailSender.send(message);
            long elapsed = System.currentTimeMillis() - start;
            return new MailStatusReport(true,
                    "Test email delivered to " + recipient, elapsed, mailHost, mailPort);
        } catch (Exception e) {
            log.error("Failed to send diagnostic email to {}", recipient, e);
            long elapsed = System.currentTimeMillis() - start;
            return new MailStatusReport(false,
                    "Dispatch error: " + e.getMessage(), elapsed, mailHost, mailPort);
        }
    }

    /* ─────────────── batch send ─────────────── */

    /**
     * Send the same HTML message to a list of recipients.
     * Uses BCC for large lists to avoid exposing every address to every recipient.
     *
     * @param recipients list of email addresses
     * @param subject    subject line
     * @param htmlBody   HTML body
     * @param batchSize  how many recipients per SMTP call (e.g., 50)
     */
    public BatchMailReport sendBatch(List<String> recipients,
                                     String subject,
                                     String htmlBody,
                                     int batchSize) {
        long start = System.currentTimeMillis();
        if (recipients == null || recipients.isEmpty()) {
            return new BatchMailReport(true, "No recipients provided", 0, 0, 0);
        }
        if (batchSize <= 0) batchSize = 50;

        int sent = 0;
        int failed = 0;
        List<String> errors = new java.util.ArrayList<>();

        for (int i = 0; i < recipients.size(); i += batchSize) {
            List<String> chunk = recipients.subList(i, Math.min(i + batchSize, recipients.size()));
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
                helper.setFrom(FROM_ADDRESS);
                helper.setTo(FROM_ADDRESS);                     // to self (or a no-reply inbox)
                helper.setBcc(chunk.toArray(String[]::new));    // hide the real list
                helper.setSubject(subject);
                helper.setText(htmlBody, true);
                helper.setSentDate(new Date());

                mailSender.send(message);
                sent += chunk.size();
                log.info("Batch sent: {} recipients in chunk {}", chunk.size(), (i / batchSize) + 1);
            } catch (Exception e) {
                failed += chunk.size();
                String msg = "Chunk " + (i / batchSize + 1) + " failed: " + e.getMessage();
                errors.add(msg);
                log.error(msg, e);
            }
        }

        long elapsed = System.currentTimeMillis() - start;
        String message = errors.isEmpty()
                ? "Broadcast complete"
                : "Broadcast finished with " + errors.size() + " failed chunk(s)";
        return new BatchMailReport(errors.isEmpty(), message, sent, failed, elapsed);
    }

    /* ─────────────── records ─────────────── */

    public record MailStatusReport(boolean connected, String message,
                                   long latencyMs, String host, int port) {}

    public record BatchMailReport(boolean success, String message,
                                  int sent, int failed, long elapsedMs) {}
}