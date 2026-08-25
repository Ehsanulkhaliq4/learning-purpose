package com.learningpurpose.userservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "temp_save_otp")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaveOtp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(nullable = false, length = 10)
    private String otp;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Builder.Default
    @Column(name = "is_used", nullable = false)
    private boolean used = false;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}
