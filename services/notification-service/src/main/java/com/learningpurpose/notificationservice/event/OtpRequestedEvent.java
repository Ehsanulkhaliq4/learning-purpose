package com.learningpurpose.notificationservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OtpRequestedEvent {
    private String email;
    private String otp;
    private Instant expiresAt;
}
